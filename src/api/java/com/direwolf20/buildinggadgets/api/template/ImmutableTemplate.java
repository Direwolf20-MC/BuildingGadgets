package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.APIReference.TemplateSerializerReference;
import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionExecutionException;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.SerialisationSupport;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.direwolf20.buildinggadgets.api.template.transaction.ITransactionOperator;
import com.direwolf20.buildinggadgets.api.util.MathUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

/**
 * An immutable implementation of {@link ITemplate}. It supports {@link ITemplateTransaction}, but those will always create a new instance.
 */
@Immutable
public final class ImmutableTemplate implements ITemplate {
    private final Long2IntMap posToStateId;
    private final Int2ObjectMap<BlockData> idToData;
    private final MaterialList requiredItems;
    private final TemplateHeader headerInfo;

    /**
     * @return A new {@code ImmutableTemplate}
     */
    public static ImmutableTemplate create() { //TODO here until this can be implemented properly
        return new ImmutableTemplate();
    }

    private ImmutableTemplate(Long2IntMap posToStateId, Int2ObjectMap<BlockData> idToData, MaterialList requiredItems, TemplateHeader headerInfo) {
        this.posToStateId = posToStateId;
        this.idToData = idToData;
        this.requiredItems = requiredItems;
        this.headerInfo = headerInfo;
    }

    private ImmutableTemplate() {
        this(new Long2IntOpenHashMap(), new Int2ObjectOpenHashMap<>(), MaterialList.empty(),
                TemplateHeader.builder(TemplateSerializerReference.IMMUTABLE_TEMPLATE_SERIALIZER_RL, Region.singleZero()).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITemplateTransaction startTransaction() {
        return new TemplateTransaction(posToStateId, idToData, requiredItems, headerInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITemplateSerializer getSerializer() {
        return SerialisationSupport.immutableTemplateSerializer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBuildView createViewInContext(IBuildContext buildContext) {
        return new BuildView(buildContext, posToStateId, idToData, requiredItems, headerInfo);
    }

    public static final class Serializer extends ForgeRegistryEntry<ITemplateSerializer> implements ITemplateSerializer {
        @Override
        public TemplateHeader createHeaderFor(ITemplate template) {
            ImmutableTemplate castTemplate = (ImmutableTemplate) template;
            return castTemplate.headerInfo;
        }

        @Override
        public CompoundNBT serialize(ITemplate template, boolean persisted) {
            return null;
        }

        @Override
        public ITemplate deserialize(CompoundNBT tagCompound, @Nullable TemplateHeader header, boolean persisted) {
            return null;
        }
    }

    private static final class BuildView implements IBuildView {
        private final IBuildContext context;
        //hold references to the fields instead of the Template in order to allow it to be garbage collected
        private final Long2IntMap posToStateId;
        private final Int2ObjectMap<BlockData> idToData;
        private final MaterialList requiredItems;
        private final TemplateHeader headerInfo;
        private BlockPos translation;

        private BuildView(IBuildContext context, Long2IntMap posToStateId, Int2ObjectMap<BlockData> idToData, MaterialList requiredItems, TemplateHeader headerInfo) {
            this.context = context;
            this.posToStateId = posToStateId;
            this.idToData = idToData;
            this.requiredItems = requiredItems;
            this.headerInfo = headerInfo;
            this.translation = BlockPos.ZERO;
        }

        @Override
        public Spliterator<PlacementTarget> spliterator() {
            return null;
        }

        @Override
        public IBuildView translateTo(BlockPos pos) {
            translation = pos;
            return this;
        }

        @Override
        public int estimateSize() {
            return posToStateId.size();
        }

        @Override
        public void close() {

        }

        @Override
        public IBuildView copy() {
            return new BuildView(context, posToStateId, idToData, requiredItems, headerInfo)
                    .translateTo(translation);
        }

        @Override
        public IBuildContext getContext() {
            return context;
        }

        @Override
        public Region getBoundingBox() {
            return headerInfo.getBoundingBox();
        }

        @Override
        public boolean mayContain(int x, int y, int z) {
            BlockPos pos = new BlockPos(x - translation.getX(), y - translation.getY(), z - translation.getZ());
            return posToStateId.containsKey(MathUtils.posToLong(pos));
        }
    }

    private static final class TemplateTransaction implements ITemplateTransaction {
        //hold references to the values instead of the TemplateItself in order to allow it to be garbage collected
        private final Long2IntMap posToStateId;
        private final Int2ObjectMap<BlockData> idToData;
        private final MaterialList requiredItems;
        private final TemplateHeader headerInfo;
        private List<ITransactionOperator> operators;

        public TemplateTransaction(Long2IntMap posToStateId, Int2ObjectMap<BlockData> idToData, MaterialList requiredItems, TemplateHeader headerInfo) {
            this.posToStateId = posToStateId;
            this.idToData = idToData;
            this.requiredItems = requiredItems;
            this.headerInfo = headerInfo;
            this.operators = new ArrayList<>();
        }

        @Override
        public ITemplateTransaction operate(ITransactionOperator operator) {
            operators.add(operator);
            return this;
        }

        @Override
        public ITemplate execute() throws TransactionExecutionException {
            return null;
        }
    }
}
