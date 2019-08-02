package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.APIReference.TemplateSerializerReference;
import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionExecutionException;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionResultExceedsTemplateSizeException.ToManyDifferentBlockDataInstances;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.SerialisationSupport;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.transaction.AbsTemplateTransaction;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.direwolf20.buildinggadgets.api.template.transaction.ITransactionExecutionContext;
import com.direwolf20.buildinggadgets.api.template.transaction.SimpleTransactionExecutionContext;
import com.direwolf20.buildinggadgets.api.util.CommonUtils;
import com.direwolf20.buildinggadgets.api.util.DelegatingSpliterator;
import com.direwolf20.buildinggadgets.api.util.MathUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collector;

/**
 * An immutable implementation of {@link ITemplate}. It supports {@link ITemplateTransaction}, but those will always create a new instance.
 */
@Immutable
public final class ImmutableTemplate implements ITemplate {
    public static final int MAX_NUM_STATES = 16_777_216; //2^24
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
        this.posToStateId = Objects.requireNonNull(posToStateId);
        this.idToData = Objects.requireNonNull(idToData);
        this.requiredItems = Objects.requireNonNull(requiredItems);
        this.headerInfo = Objects.requireNonNull(headerInfo);
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
        @Nullable
        private MaterialList requiredItems;
        private final TemplateHeader headerInfo;
        private BlockPos translation;

        private BuildView(IBuildContext context, Long2IntMap posToStateId, Int2ObjectMap<BlockData> idToData, @Nullable MaterialList requiredItems, TemplateHeader headerInfo) {
            this.context = context;
            this.posToStateId = posToStateId;
            this.idToData = idToData;
            this.requiredItems = requiredItems;
            this.headerInfo = headerInfo;
            this.translation = BlockPos.ZERO;
        }

        @Override
        public Spliterator<PlacementTarget> spliterator() {
            return new BuildSpliterator(posToStateId.long2IntEntrySet().spliterator(), idToData);
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
            //do nothing on close - this Template is immutable and therefore does not need to care about modification
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

        @Override
        public MaterialList estimateRequiredItems(@Nullable Vec3d simulatePos) {
            if (requiredItems == null)
                requiredItems = IBuildView.super.estimateRequiredItems(simulatePos);
            return requiredItems;
        }

        private static final class BuildSpliterator extends DelegatingSpliterator<Long2IntMap.Entry, PlacementTarget> {
            private final Int2ObjectMap<BlockData> idToData;

            public BuildSpliterator(Spliterator<Entry> idSpliterator, Int2ObjectMap<BlockData> idToData) {
                super(idSpliterator);
                this.idToData = idToData; //no sync whatsoever - Immutable!
            }

            @Override
            @Nullable
            public Spliterator<PlacementTarget> trySplit() {
                Spliterator<Entry> split = getOther().trySplit();
                if (split != null)
                    return new BuildSpliterator(split, idToData);
                return null;
            }

            @Override
            protected boolean advance(Long2IntMap.Entry object, Consumer<? super PlacementTarget> action) {
                BlockPos constructed = MathUtils.posFromLong(object.getLongKey());
                BlockData data = idToData.get(object.getIntValue());
                action.accept(new PlacementTarget(constructed, data));
                return true;
            }
        }
    }

    private static final class TemplateTransaction extends AbsTemplateTransaction {
        private static final int PARALLEL_THRESHOLD = 512; //8^3
        //hold references to the values instead of the TemplateItself in order to allow it to be garbage collected
        private Long2IntMap posToStateId;
        private Int2ObjectMap<BlockData> idToData;
        private MaterialList requiredItems;
        private TemplateHeader headerInfo;
        //--------cache start------
        @Nullable
        private Map<BlockPos, BlockData> posToData;
        private Map<BlockData, Set<BlockPos>> dataToPos;

        public TemplateTransaction(Long2IntMap posToStateId, Int2ObjectMap<BlockData> idToData, MaterialList requiredItems, TemplateHeader headerInfo) {
            super();
            this.posToStateId = posToStateId;
            this.idToData = idToData;
            this.requiredItems = requiredItems;
            this.headerInfo = headerInfo;
            this.posToData = null;
            this.dataToPos = null;
        }

        @Override
        protected ITransactionExecutionContext createContext() {
            return SimpleTransactionExecutionContext.builder()
                    .size(posToStateId.size())
                    .build(headerInfo.getBoundingBox());
        }

        @Override
        protected void mergeCreated(ITransactionExecutionContext exContext, OperatorOrdering ordering, Map<BlockPos, BlockData> created) {
            ensureReverseMappingCreated();
            assert posToData != null;
            assert dataToPos != null;
            for (Map.Entry<BlockPos, BlockData> entry : created.entrySet()) {
                BlockData cur = posToData.get(entry.getKey());
                posToData.put(entry.getKey(), entry.getValue());
                Set<BlockPos> positions = dataToPos.computeIfAbsent(entry.getValue(), k -> new HashSet<>());
                positions.add(entry.getKey());
                if (cur != null) { //replace op
                    Set<BlockPos> curPositions = dataToPos.get(cur);
                    if (curPositions != null) {
                        if (curPositions.size() <= 1)
                            dataToPos.remove(cur);
                        else
                            curPositions.remove(entry.getKey());
                    }
                }
            }
        }

        @Override
        protected void transformAllData(ITransactionExecutionContext exContext, OperatorOrdering ordering, DataTransformer dataTransformer) throws TransactionExecutionException {
            ensureReverseMappingCreated();
            assert posToData != null;
            assert dataToPos != null;
            Map<BlockData, Set<BlockPos>> newDataToPositionMapping = new HashMap<>();
            for (Map.Entry<BlockData, Set<BlockPos>> entry : dataToPos.entrySet()) {
                BlockData newData = dataTransformer.transformData(entry.getKey());
                Set<BlockPos> positions = newDataToPositionMapping.computeIfAbsent(newData, k -> new HashSet<>());
                positions.addAll(entry.getValue());
                if (! newData.equals(entry.getKey())) {
                    for (BlockPos pos : entry.getValue()) {
                        posToData.put(pos, newData);
                    }
                }
            }
            dataToPos = newDataToPositionMapping;
        }

        @Override
        protected void transformAllPositions(ITransactionExecutionContext exContext, OperatorOrdering ordering, PositionTransformer positionTransformer) throws TransactionExecutionException {
            ensureReverseMappingCreated();
            assert posToData != null;
            assert dataToPos != null;
            Map<BlockPos, BlockData> newPosToDataMap = new HashMap<>();
            for (Map.Entry<BlockPos, BlockData> entry : posToData.entrySet()) {
                BlockPos newPos = positionTransformer.transformPos(entry.getKey(), entry.getValue());
                newPosToDataMap.put(newPos, entry.getValue());
                Set<BlockPos> positions = dataToPos.get(entry.getValue());
                assert positions != null && ! positions.isEmpty(); //we know that the data was present in the mapping and has an assigned pos
                positions.remove(entry.getKey());
                positions.add(newPos);
            }
            posToData = newPosToDataMap;
        }

        @Override
        protected void updateHeader(ITransactionExecutionContext exContext, OperatorOrdering ordering, HeaderTransformer transformer) throws TransactionExecutionException {
            headerInfo = transformer.transformHeader(headerInfo);
        }

        @Override
        protected ITemplate createTemplate(ITransactionExecutionContext exContext, OperatorOrdering ordering, @Nullable IBuildContext context, boolean changed) throws TransactionExecutionException {
            if (! changed)
                return new ImmutableTemplate(posToStateId, idToData, requiredItems, headerInfo);
            assert posToData != null;
            posToStateId = new Long2IntOpenHashMap(posToData.size());
            createPosToState(createIdToData(), context);
            return createTemplate(exContext, ordering, context, false);
        }

        private void ensureReverseMappingCreated() {
            if (posToData != null && dataToPos != null)
                return;
            posToData = new HashMap<>(posToStateId.size());
            dataToPos = new HashMap<>(idToData.size());
            for (Long2IntMap.Entry entry : posToStateId.long2IntEntrySet()) {
                BlockPos pos = MathUtils.posFromLong(entry.getLongKey());
                BlockData data = idToData.get(entry.getIntValue());
                posToData.put(pos, data);
                Set<BlockPos> positions = dataToPos.computeIfAbsent(data, k -> new HashSet<>());
                positions.add(pos);
            }
            //free memory if possible
            posToStateId = null;
            idToData = null;
            requiredItems = null;
        }

        private Object2IntMap<BlockData> createIdToData() throws TransactionExecutionException {
            assert dataToPos != null;
            idToData = new Int2ObjectOpenHashMap<>(dataToPos.size());
            Object2IntMap<BlockData> reverse = new Object2IntOpenHashMap<>(dataToPos.size());
            int curId = 0;
            for (BlockData data : dataToPos.keySet()) {
                idToData.put(curId, data);
                reverse.put(data, curId++);
                if (curId > MAX_NUM_STATES)
                    throw new ToManyDifferentBlockDataInstances(
                            "Cannot have more then 2^24 different types of BlockData in one ImmutableTemplate!", this);
            }
            dataToPos = null; //free memory
            return reverse;
        }

        private void createPosToState(Object2IntMap<BlockData> reverseMap, @Nullable IBuildContext context) {
            assert posToData != null;
            posToStateId = new Long2IntOpenHashMap(posToData.size());
            MaterialList.Builder builder = context != null ? MaterialList.builder() : null;
            MutableBlockPos smallest = getSmallest(); //linear-Time Operation - may be multithreaded though!!!
            int maxx, maxy, maxz;
            maxx = maxy = maxz = 0;
            for (Map.Entry<BlockPos, BlockData> entry : posToData.entrySet()) {
                //ensure that positions are shifted as much as possible towards (0, 0, 0) => There will be at least one position for each axis which has the value equal to 0
                BlockPos resPos = entry.getKey().subtract(smallest);
                posToStateId.put(MathUtils.posToLong(resPos), reverseMap.getInt(entry.getValue()));
                if (context != null) {
                    PlacementTarget target = new PlacementTarget(resPos, entry.getValue());
                    PlayerEntity player = context.getBuildingPlayer();
                    BlockRayTraceResult targetRes = player != null ? CommonUtils.fakeRayTrace(player.posX, player.posY, player.posZ, resPos) : null;
                    builder.addAll(target.getRequiredItems(context, targetRes).getRequiredItems());
                }
                maxx = Math.max(maxx, resPos.getX());
                maxy = Math.max(maxy, resPos.getY());
                maxz = Math.max(maxz, resPos.getZ());
            }
            requiredItems = builder != null ? builder.build() : null;
            headerInfo = TemplateHeader.builderOf(headerInfo)
                    .bounds(new Region(0, 0, 0, maxx, maxy, maxz))
                    .build();
        }

        private MutableBlockPos getSmallest() {
            assert posToData != null;
            Set<BlockPos> keySet = posToData.keySet();
            return (keySet.size() >= PARALLEL_THRESHOLD ? keySet.parallelStream() : keySet.stream()).collect(Collector.of(
                    () -> new MutableBlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
                    (mutable, pos) -> {
                        mutable.func_223471_o(Math.min(mutable.getX(), pos.getX()));
                        mutable.setY(Math.min(mutable.getY(), pos.getY()));
                        mutable.func_223472_q(Math.min(mutable.getZ(), pos.getZ()));
                    },
                    (m1, m2) -> new MutableBlockPos(
                            Math.min(m1.getX(), m2.getX()),
                            Math.min(m1.getY(), m2.getY()),
                            Math.min(m1.getZ(), m2.getZ())),
                    Collector.Characteristics.UNORDERED));
        }
    }
}
