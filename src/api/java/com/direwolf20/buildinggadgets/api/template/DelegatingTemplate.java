package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.building.ITemplateView;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.google.common.collect.Multiset;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An {@link ITemplate} implementation delegating all Actions through to another {@link ITemplate}. All necessary synchronisation is performed.
 * <p>
 * This class is especially useful as a base class for Decorator or Wrapper implementations.
 */
public class DelegatingTemplate implements ITemplate {
    public static ITemplate unwrap(ITemplate template) {
        if (template instanceof DelegatingTemplate)
            return unwrap(((DelegatingTemplate) template).getDelegate());
        return template;
    }

    public static DelegatingTemplate createUnwrapped(ITemplate template) {
        return new DelegatingTemplate(unwrap(template));
    }

    private ITemplate delegate;

    public DelegatingTemplate(ITemplate delegate) {
        this.delegate = Objects.requireNonNull(delegate);
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
        return getDelegate().startTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Multiset<IUniqueItem> estimateRequiredItems() {
        return getDelegate().estimateRequiredItems();
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
    public ITemplateView createViewInContext(IBuildContext buildContext) {
        return getDelegate().createViewInContext(buildContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int estimateSize() {
        return getDelegate().estimateSize();
    }

    /**
     * @return The Delegate of this {@code DelegatingTemplate}.
     * @implNote Subclasses can assume safely, that this class does not use the instance Field directly, but instead refers to this Method internally.
     */
    protected ITemplate getDelegate() {
        return delegate;
    }

    /**
     * @param delegate The new delegate to use.
     * @throws NullPointerException if delegate is null.
     * @implNote Subclasses can assume safely, that this class does not use the instance Field directly, but instead refers to this Method internally.
     * @implNote This Method is not called from within the constructor!
     */
    protected void setDelegate(ITemplate delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }
}
