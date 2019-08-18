package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.Registries;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.exceptions.*;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.IBuildOpenOptions.OpenType;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.direwolf20.buildinggadgets.api.template.transaction.ITransactionOperator;
import com.direwolf20.buildinggadgets.api.template.transaction.ReplaceDelegateOperator;
import com.direwolf20.buildinggadgets.api.util.DelegatingSpliterator;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.direwolf20.buildinggadgets.api.util.RegistryUtils;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * An {@link ITemplate} implementation delegating all Actions through to another {@link ITemplate}. All necessary synchronisation is performed.
 * <p>
 * This class is especially useful as a base class for Decorator or Wrapper implementations.
 */
public class DelegatingTemplate implements ITemplate {
    public static ITemplate unwrap(ITemplate template) {
        if (template instanceof DelegatingTemplate) {
            DelegatingTemplate castTemplate = (DelegatingTemplate) template;
            ITemplate delegate;
            synchronized (castTemplate.getDelegateLock()) {
                delegate = castTemplate.getDelegate();
            }
            return unwrap(delegate);
        }
        return template;
    }

    public static DelegatingTemplate createUnwrapped(ITemplate template) {
        return new DelegatingTemplate(unwrap(template));
    }

    private ITemplate delegate;
    private Object delegateLock;
    private Set<IBuildView> activeViews;
    private boolean transactionActive;

    public DelegatingTemplate(ITemplate delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.delegateLock = new Object();
        this.activeViews = Collections.newSetFromMap(new IdentityHashMap<>());
        this.transactionActive = false;
    }

    public DelegatingTemplate() {
        this(ImmutableTemplate.create());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ITemplateTransaction startTransaction() {
        ITemplateTransaction delegateTransaction;
        synchronized (getDelegateLock()) {
            if (isTransactionActive()) //if there is already a Transaction active - don't go through the headaches of permitting another one
                return null;
            delegateTransaction = getDelegate().startTransaction();
            if (delegateTransaction == null)
                return null;
            markTransactionActive();
        }
        return new DelegatingTransaction(this, delegateTransaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITemplateSerializer getSerializer() {
        return getDelegate().getSerializer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBuildView createViewInContext(IBuildOpenOptions openOptions) {
        switch (openOptions.getOpenType()) {
            case DEFAULT:
                return new DelegatingBuildView(this, openOptions);
            case IF_NO_TRANSACTION_OPEN:
                synchronized (getDelegateLock()) {
                    if (! transactionActive)
                        return new DelegatingBuildView(this, openOptions);
                    else
                        return null;
                }
            default:
                return null;
        }
    }

    /**
     * This Method must be synchronized externally on {@link #getDelegateLock()}
     * @return The Delegate of this {@code DelegatingTemplate}.
     * @implNote Subclasses can assume safely, that this class does not use the instance Field directly, but instead refers to this Method internally.
     */
    protected ITemplate getDelegate() {
        return delegate;
    }

    /**
     * This Method must be synchronized externally on {@link #getDelegateLock()}
     * @param delegate The new delegate to use.
     * @throws NullPointerException if delegate is null.
     * @implNote Subclasses can assume safely, that this class does not use the instance Field directly, but instead refers to this Method internally.
     *           This Method is not called from within the constructor!
     */
    protected void setDelegate(ITemplate delegate) {
        this.delegate = Objects.requireNonNull(delegate, "Cannot have a null delegate!");
    }

    protected Object getDelegateLock() {
        return delegateLock;
    }

    /**
     * This Method must be synchronized externally on {@link #getDelegateLock()}
     *
     * @return The currently active {@link IBuildView IBuildViews}
     */
    protected Set<IBuildView> getActiveViews() {
        return activeViews;
    }

    protected boolean isTransactionActive() {
        return transactionActive;
    }

    protected void markTransactionActive() {
        this.transactionActive = true;
    }

    protected void markTransactionFinished() {
        this.transactionActive = false;
    }

    public static class DelegatingTemplateSerializer extends ForgeRegistryEntry<ITemplateSerializer> implements ITemplateSerializer {
        @Override
        public TemplateHeader createHeaderFor(ITemplate template) {
            assert getRegistryName() != null;
            DelegatingTemplate castTemplate = (DelegatingTemplate) template;
            ITemplate delegateTemplate;
            synchronized (castTemplate.getDelegateLock()) {
                delegateTemplate = castTemplate.getDelegate();
            }
            TemplateHeader delegateHeader = delegateTemplate.getSerializer().createHeaderFor(delegateTemplate);
            return TemplateHeader.builderOf(delegateHeader, getRegistryName(), delegateHeader.getBoundingBox())
                    .build();
        }

        @Override
        public CompoundNBT serialize(ITemplate template, boolean persisted) {
            DelegatingTemplate castTemplate = (DelegatingTemplate) template;
            CompoundNBT nbt = new CompoundNBT();
            ITemplate delegate;
            synchronized (castTemplate.getDelegateLock()) {
                delegate = castTemplate.getDelegate();
            }
            assert delegate.getSerializer().getRegistryName() != null;
            if (persisted)
                nbt.putString(NBTKeys.KEY_SERIALIZER, delegate.getSerializer().getRegistryName().toString());
            else
                nbt.putInt(NBTKeys.KEY_SERIALIZER, RegistryUtils.getId(Registries.getTemplateSerializers(), delegate.getSerializer()));
            nbt.put(NBTKeys.KEY_DATA, delegate.getSerializer().serialize(delegate, persisted));
            return nbt;
        }

        @Override
        public ITemplate deserialize(CompoundNBT tagCompound, @Nullable TemplateHeader header, boolean persisted) {
            ITemplateSerializer serializer = null;
            if (tagCompound.contains(NBTKeys.KEY_SERIALIZER, NBT.TAG_INT))
                serializer = RegistryUtils.getById(Registries.getTemplateSerializers(), tagCompound.getInt(NBTKeys.KEY_SERIALIZER));
            if (serializer == null && tagCompound.contains(NBTKeys.KEY_SERIALIZER, NBT.TAG_STRING)) {
                ResourceLocation serializerId = new ResourceLocation(tagCompound.getString(NBTKeys.KEY_SERIALIZER));
                serializer = Registries.getTemplateSerializers().getValue(serializerId);
            }
            Preconditions.checkArgument(serializer != null,
                    "Cannot construct a Delegating Template with unknown Delegate Serializer. Expected Int or String '" + NBTKeys.KEY_SERIALIZER + "' to be present!");
            assert serializer.getRegistryName() != null;
            CompoundNBT data = tagCompound.getCompound(NBTKeys.KEY_DATA);
            ITemplate delegate = serializer.deserialize(
                    data,
                    header != null ? TemplateHeader.builderOf(header, serializer.getRegistryName(), header.getBoundingBox()).build() : null,
                    persisted);
            return new DelegatingTemplate(delegate);
        }
    }

    public static class DelegatingBuildView implements IBuildView {
        private IBuildView view;
        private IBuildContext context;
        private DelegatingTemplate template;

        protected DelegatingBuildView(DelegatingTemplate template, IBuildOpenOptions openOptions) {
            synchronized (template.getDelegateLock()) {
                template.getActiveViews().add(this);
                view = template.getDelegate().createViewInContext(SimpleBuildOpenOptions
                        .builderCopyOf(openOptions)
                        .openType(OpenType.DEFAULT)
                        .build());
            }
            this.template = template;
            this.context = openOptions.getContext();
        }

        @Override
        public Spliterator<PlacementTarget> spliterator() {
            if (template == null || view == null)
                throw new UnsupportedOperationException("Cannot create a Spliterator for an already closed IBuildView!");
            return new DelegatingBuildSpliterator(view.spliterator());
        }

        @Override
        public IBuildView translateTo(BlockPos pos) {
            validateOpen();
            return view.translateTo(pos);
        }

        @Override
        public int estimateSize() {
            validateOpen();
            return view.estimateSize();
        }

        @Override
        public void close() throws TemplateException {
            if (template == null || view == null)
                throw new TemplateViewAlreadyClosedException("Cannot close already closed view!");
            try {
                synchronized (template.getDelegateLock()) {
                    template.getActiveViews().remove(this);
                    view.close();
                }
            } finally {
                template = null;
                view = null;
            }
        }

        @Override
        public IBuildView copy() {
            validateOpen();
            return new DelegatingBuildView(template, SimpleBuildOpenOptions
                    .builder()
                    .context(getContext())
                    .build());
        }

        @Override
        public IBuildContext getContext() {
            return context;
        }

        @Override
        public Region getBoundingBox() {
            validateOpen();
            return view.getBoundingBox();
        }

        @Override
        public boolean mayContain(int x, int y, int z) {
            validateOpen();
            return view.mayContain(x, y, z);
        }

        @Override
        public MaterialList estimateRequiredItems(@Nullable Vec3d simulatePos) {
            validateOpen();
            return view.estimateRequiredItems(simulatePos);
        }

        protected void validateOpen() {
            Preconditions.checkState(getTemplate() != null && getView() != null, "Cannot access already closed BuildView!");
        }

        @Nullable
        protected IBuildView getView() {
            return view;
        }

        @Nullable
        protected DelegatingTemplate getTemplate() {
            return template;
        }

        public class DelegatingBuildSpliterator extends DelegatingSpliterator<PlacementTarget, PlacementTarget> {
            protected DelegatingBuildSpliterator(Spliterator<PlacementTarget> other) {
                super(other);
            }

            @Override
            protected boolean advance(PlacementTarget object, Consumer<? super PlacementTarget> action) {
                action.accept(object);
                return true;
            }

            @Override
            @Nullable
            public Spliterator<PlacementTarget> trySplit() {
                Spliterator<PlacementTarget> other = getOther().trySplit();
                if (other != null)
                    return new DelegatingBuildSpliterator(other);
                return null;
            }
        }
    }

    public static class DelegatingTransaction implements ITemplateTransaction {
        private DelegatingTemplate template;
        private ITemplateTransaction transaction;
        private List<ITransactionOperator> operators;
        private ReplaceDelegateOperator replaceOp;

        protected DelegatingTransaction(DelegatingTemplate template, ITemplateTransaction transaction) {
            this.template = template;
            this.transaction = transaction;
            this.operators = new LinkedList<>();
            this.replaceOp = null;
        }

        @Override
        public ITemplateTransaction operate(ITransactionOperator operator) {
            Preconditions.checkState(transaction != null && template != null, "Transaction has already been executed!");
            if (operator instanceof ReplaceDelegateOperator)
                replaceOp = (ReplaceDelegateOperator) operator;
            else
                operators.add(Objects.requireNonNull(operator, "Cannot have a null Operator!"));
            return this;
        }

        @Override
        public ITemplate execute(@Nullable IBuildContext context) throws TransactionExecutionException {
            if (transaction == null || template == null)
                throw new TransactionInvalidException("Cannot execute Transaction Twice!");
            if (replaceOp != null)
                applyResultAndReplaceOrComplete(replaceOp.getNewDelegate(), ! operators.isEmpty());
            try {
                if (! operators.isEmpty()) {
                    for (ITransactionOperator operator : operators) {
                        transaction.operate(operator);
                    }
                    ITemplate result = transaction.execute(context);
                    return applyResultAndReplaceOrComplete(result, false);
                } else if (replaceOp != null)
                    return invalidateNoFinish();
                else
                    return invalidate(true);
            } catch (Exception e) {
                invalidate(true);
                throw e;
            }
        }

        protected ITemplate applyResultAndReplaceOrComplete(ITemplate newDelegate, boolean replaceTransaction) throws TransactionExecutionException {
            if (replaceTransaction)
                transaction.execute(null);
            synchronized (template.getDelegateLock()) {
                if (! template.getActiveViews().isEmpty()) {
                    invalidate(false);
                    throw new ConcurrentTransactionExecutionException("Cannot apply delegate Template whilst a BuildView is present!");
                }
                template.setDelegate(newDelegate);
                if (replaceTransaction)
                    transaction = newDelegate.startTransaction();
                else {
                    return invalidate(false);
                }
            }
            return template;
        }

        protected ITemplate invalidate(boolean requireSync) {
            if (requireSync)
                synchronized (template.getDelegateLock()) {
                    template.markTransactionFinished();
                }
            else
                template.markTransactionFinished();
            return invalidateNoFinish();
        }

        protected ITemplate invalidateNoFinish() {
            ITemplate res = template;
            template = null;
            transaction = null;
            return res;
        }
    }
}
