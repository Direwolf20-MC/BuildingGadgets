package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.util.CommonUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public final class TemplateTransactions {
    private TemplateTransactions() {}

    public static ITransactionOperator copyOperatorUnsafe(Map<BlockPos, BlockData> map) {
        return new CopyOperator(Objects.requireNonNull(map, "Cannot create a Copy Operator from a null map!"));
    }

    public static ITransactionOperator copyOperator(Map<BlockPos, BlockData> map) {
        return new CopyOperator(ImmutableMap.copyOf(Objects.requireNonNull(map, "Cannot create a Copy Operator from a null map!")));
    }

    public static ITransactionOperator copyOperator(Iterable<PlacementTarget> iterable) {
        return new CopyOperator(CommonUtils.targetsToMap(Objects.requireNonNull(iterable, "Cannot create a Copy Operator from a null map!")));
    }

    private static final class CopyOperator extends AbsSingleRunTransactionOperator {
        private Iterator<BlockPos> positions;
        private final Map<BlockPos, BlockData> dataMap;

        private CopyOperator(Map<BlockPos, BlockData> dataMap) {
            super(EnumSet.of(TransactionOperation.CREATE_DATA));
            this.dataMap = dataMap;
            this.positions = dataMap.keySet().iterator();
        }

        @Nullable
        @Override
        public BlockPos createPos(ITransactionExecutionContext context) {
            super.createPos(context);
            if (positions != null && positions.hasNext())
                return positions.next();
            positions = null;
            return null;
        }

        @Nullable
        @Override
        public BlockData createDataForPos(ITransactionExecutionContext context, BlockPos pos) {
            super.createDataForPos(context, pos);
            return getDataMap().get(pos);
        }

        private Map<BlockPos, BlockData> getDataMap() {
            return dataMap;
        }
    }

    /**
     * Creates a {@link #replaceOperator(Map) Replace-Operator} without performing any iterations on the given map.
     * Notice that it is assumed that the given map is not going to change until the Operator is executed and that if the Operator
     * is executed on another Thread that the given Map must be Thread-Safe.
     *
     * @param map The map which provides the data to be in the resulting Template. Should not be modified after being passed into this Method.
     * @return An {@link ITransactionOperator} as described in {@link #replaceOperator(Iterable)}
     * @throws NullPointerException if map is null
     * @see #replaceOperator(Map)
     */
    public static ITransactionOperator replaceOperatorUnsafe(Map<BlockPos, BlockData> map) {
        return new ReplaceOperator(Objects.requireNonNull(map, "Cannot construct a Replace Operator from a null map!"));
    }

    /**
     * Creates a Replace Operator from the given data map. Acts like {@link #copyOperator(Map)} except that all positions that are not in the map will be
     * removed via {@link ITransactionOperator#transformTarget(ITransactionExecutionContext, PlacementTarget)}.
     *
     * @param map The map which provides the data to be in the resulting Template. {@link ImmutableMap#copyOf(Map)} will be called on this to ensure a
     *            Thread-Safe and unmodifiable copy.
     * @return An {@link ITransactionOperator} attempting to replace all data in the resulting {@link com.direwolf20.buildinggadgets.api.template.ITemplate}
     * @throws NullPointerException if map is null
     */
    public static ITransactionOperator replaceOperator(Map<BlockPos, BlockData> map) {
        return new ReplaceOperator(ImmutableMap.copyOf(Objects.requireNonNull(map, "Cannot construct a Replace Operator from a null map!")));
    }

    /**
     * Creates a Replace Operator from the given {@link Iterable} of {@link PlacementTarget PlacementTargets}.
     *
     * @param targets An {@link Iterable} of {@link PlacementTarget} to be converted into a map
     * @return An {@link ITransactionOperator} as described in {@link #replaceOperator(Map)}
     * @throws NullPointerException     if targets is null
     * @throws IllegalArgumentException if the positions returned by the {@link PlacementTarget#getPos() targets} aren't unique
     * @see #replaceOperator(Map)
     */
    public static ITransactionOperator replaceOperator(Iterable<PlacementTarget> targets) {
        return new ReplaceOperator(CommonUtils.targetsToMap(Objects.requireNonNull(targets, "Cannot construct a Replace Operator from a null Iterable!")));
    }

    private static final class ReplaceOperator extends AbsSingleRunTransactionOperator {
        private final CopyOperator copyOperator;

        private ReplaceOperator(Map<BlockPos, BlockData> dataMap) {
            super(EnumSet.of(TransactionOperation.CREATE_DATA, TransactionOperation.TRANSFORM_TARGET));
            this.copyOperator = new CopyOperator(dataMap);
        }

        @Nullable
        @Override
        public BlockPos createPos(ITransactionExecutionContext context) {
            super.createPos(context);
            return copyOperator.createPos(context);
        }

        @Nullable
        @Override
        public BlockData createDataForPos(ITransactionExecutionContext context, BlockPos pos) {
            super.createDataForPos(context, pos);
            return copyOperator.createDataForPos(context, pos);
        }

        @Nullable
        @Override
        public PlacementTarget transformTarget(ITransactionExecutionContext context, PlacementTarget target) {
            super.transformTarget(context, target);
            if (copyOperator.getDataMap().containsKey(target.getPos()))
                return target;
            return null;
        }
    }

    public static ITransactionOperator replaceDataOperatorUnsafe(Map<BlockData, BlockData> map) {
        return new ReplaceDataOperator(Objects.requireNonNull(map, "Cannot construct a ReplaceOperator from a null map!"));
    }

    public static ITransactionOperator replaceDataOperator(Map<BlockData, BlockData> map) {
        return new ReplaceDataOperator(map instanceof ImmutableMap ? map : new HashMap<>(Objects.requireNonNull(map, "Cannot construct a ReplaceOperator from a null map!")));
    }

    private static final class ReplaceDataOperator extends AbsSingleRunTransactionOperator {
        private final Map<BlockData, BlockData> dataMap;

        public ReplaceDataOperator(Map<BlockData, BlockData> dataMap) {
            super(EnumSet.of(TransactionOperation.TRANSFORM_DATA));
            this.dataMap = dataMap;
        }

        @Nullable
        @Override
        public BlockData transformData(ITransactionExecutionContext context, BlockData data) {
            return super.transformData(context, dataMap.getOrDefault(data, data));
        }
    }

    public ITransactionOperator removeHeaderOperator() {
        return new HeaderOperator(null, null);
    }

    public ITransactionOperator replaceHeaderOperator(@Nullable String name, @Nullable String author) {
        return new HeaderOperator(name, author);
    }

    private static final class HeaderOperator extends AbsSingleRunTransactionOperator {
        @Nullable
        private final String newName;
        @Nullable
        private final String newAuthor;

        private HeaderOperator(@Nullable String newName, @Nullable String newAuthor) {
            super(EnumSet.of(TransactionOperation.TRANSFORM_HEADER));
            this.newName = newName;
            this.newAuthor = newAuthor;
        }

        @Override
        public TemplateHeader transformHeader(ITransactionExecutionContext context, TemplateHeader header) {
            header = TemplateHeader.builderOf(header)
                    .name(newName)
                    .author(newAuthor)
                    .build();
            return super.transformHeader(context, header);
        }
    }

    public static ITransactionOperator headerNameOperator(@Nullable String name) {
        return new AddToHeaderOperator(name, null);
    }

    public static ITransactionOperator headerAuthorOperator(@Nullable String author) {
        return new AddToHeaderOperator(null, author);
    }

    public static ITransactionOperator headerOperator(@Nullable String name, @Nullable String author) {
        return new AddToHeaderOperator(name, author);
    }

    private static final class AddToHeaderOperator extends AbsSingleRunTransactionOperator {
        @Nullable
        private final String newName;
        @Nullable
        private final String newAuthor;

        public AddToHeaderOperator(@Nullable String newName, @Nullable String newAuthor) {
            super(EnumSet.of(TransactionOperation.TRANSFORM_HEADER));
            this.newName = newName;
            this.newAuthor = newAuthor;
        }

        @Override
        public TemplateHeader transformHeader(ITransactionExecutionContext context, TemplateHeader header) {
            if (newName != null || newAuthor != null)
                header = TemplateHeader.builderOf(header)
                        .name(newName != null ? newName : header.getName())
                        .author(newAuthor != null ? newAuthor : header.getAuthor())
                        .build();
            return super.transformHeader(context, header);
        }
    }
}
