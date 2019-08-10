package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.util.CommonUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static com.direwolf20.buildinggadgets.api.util.CommonUtils.inputIfNonNullFunction;

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

    /**
     * Identical to calling {@code rotateOperator(Axis.Y, rotation)}.
     *
     * @see #rotateOperator(Axis, Rotation)
     */
    public static ITransactionOperator rotateOperator(Rotation rotation) {
        return new RotateOperator(Objects.requireNonNull(rotation, "Cannot rotate without an Rotation to apply!"));
    }

    /**
     * Notice, that MC's {@link net.minecraft.block.BlockState} only support rotation around the y-Axis! Therefore they cannot be
     * rotated around x and z Axis and in this case they'll just be rotated as is.
     *
     * @param axis     The axis to rotate around
     * @param rotation The rotation to apply
     * @return An {@link ITransactionOperator} rotating all PlacementTargets around the specified axis with the specified rotation.
     */
    public static ITransactionOperator rotateOperator(Axis axis, Rotation rotation) {
        return new RotateOperator(Objects.requireNonNull(axis, "Cannot rotate without an axis to rotate around!"),
                Objects.requireNonNull(rotation, "Cannot rotate without an Rotation to apply!"));
    }

    /**
     * Notice that mirroring can only be performed along the X and Z Axis - otherwise this Operator will have no effect at all!
     *
     * @param axis The Axis to mirror along
     * @return An {@link ITransactionOperator} mirroring along the specified Axis.
     */
    public static ITransactionOperator mirrorOperator(Axis axis) {
        return new MirrorOperator(axis);
    }

    private static final class MirrorOperator extends AbsSingleRunTransactionOperator {
        private int xFac;
        private int zFac;
        private Mirror mirror;

        private MirrorOperator(Axis axis) {
            super(axis != Axis.Y ? EnumSet.of(TransactionOperation.TRANSFORM_TARGET) : EnumSet.noneOf(TransactionOperation.class));
            xFac = zFac = 1;
            switch (axis) {
                case X:
                    mirror = Mirror.LEFT_RIGHT;
                    zFac = - 1;
                    break;
                case Z:
                    mirror = Mirror.FRONT_BACK;
                    xFac = - 1;
                    break;
                default:
                    mirror = Mirror.NONE;
            }
        }

        @Nullable
        @Override
        public PlacementTarget transformTarget(ITransactionExecutionContext context, PlacementTarget target) {
            int x = target.getPos().getX() * xFac;
            int y = target.getPos().getZ() * zFac;
            return super.transformTarget(context, new PlacementTarget(new BlockPos(x, target.getPos().getY(), y), target.getData().mirror(mirror)));
        }
    }

    public static ITransactionOperator modifyHeaderAuthorOperator(Function<String, String> author) {
        return modifyHeaderOperator(Function.identity(), author);
    }

    public static ITransactionOperator modifyHeaderNameOperator(Function<String, String> name) {
        return modifyHeaderOperator(name, Function.identity());
    }

    public static ITransactionOperator modifyHeaderOperator(Function<String, String> name, Function<String, String> author) {
        return new HeaderOperator(
                Objects.requireNonNull(name, "Cannot construct Header modification with null name Function!"),
                Objects.requireNonNull(author, "Cannot construct Header modification with null author Function!"));
    }

    public static ITransactionOperator headerNameOperator(@Nullable String name) {
        return modifyHeaderNameOperator(inputIfNonNullFunction(name));
    }

    public static ITransactionOperator headerAuthorOperator(@Nullable String author) {
        return modifyHeaderAuthorOperator(inputIfNonNullFunction(author));
    }

    public static ITransactionOperator headerOperator(@Nullable String name, @Nullable String author) {
        return modifyHeaderOperator(inputIfNonNullFunction(name), inputIfNonNullFunction(author));
    }

    private static final class HeaderOperator extends AbsSingleRunTransactionOperator {
        private final Function<String, String> newName;
        private final Function<String, String> newAuthor;

        private HeaderOperator(Function<String, String> newName, Function<String, String> newAuthor) {
            super(EnumSet.of(TransactionOperation.TRANSFORM_HEADER));
            this.newName = newName;
            this.newAuthor = newAuthor;
        }

        @Override
        public TemplateHeader transformHeader(ITransactionExecutionContext context, TemplateHeader header) {
            header = TemplateHeader.builderOf(header)
                    .name(newName.apply(header.getName()))
                    .author(newAuthor.apply(header.getAuthor()))
                    .build();
            return super.transformHeader(context, header);
        }
    }

    /**
     * Useful in cases where one Operator needs updated Context data from another Operator - just put it one pass after the other
     *
     * @return an {@link ITransactionOperator} which delegates through to the other Operator after passesToShift many passes.
     */
    public static ITransactionOperator shiftingOperator(ITransactionOperator other, int passesToShift) {
        return new ShiftingOperator(other, passesToShift);
    }

    private static final class ShiftingOperator implements ITransactionOperator {
        private final ITransactionOperator other;
        private int toShiftCounter;
        private TransactionOperation lastOperation;

        public ShiftingOperator(ITransactionOperator other, int toShiftAmount) {
            this.other = other;
            this.toShiftCounter = toShiftAmount + 1;
            this.lastOperation = null;
        }

        @Nullable
        @Override
        public BlockPos createPos(ITransactionExecutionContext context) {
            setLastOperation(TransactionOperation.CREATE_DATA);
            if (lastOperation == TransactionOperation.CREATE_DATA)
                -- toShiftCounter;
            if (toShiftCounter <= 0)
                return other.createPos(context);
            return null;
        }

        @Nullable
        @Override
        public BlockData createDataForPos(ITransactionExecutionContext context, BlockPos pos) {
            if (toShiftCounter <= 0)
                return other.createDataForPos(context, pos);
            return null;
        }

        @Override
        public TemplateHeader transformHeader(ITransactionExecutionContext context, TemplateHeader header) {
            setLastOperation(TransactionOperation.TRANSFORM_HEADER);
            if (lastOperation == TransactionOperation.TRANSFORM_HEADER)
                -- toShiftCounter;
            if (toShiftCounter <= 0)
                return other.transformHeader(context, header);
            return header;
        }

        @Nullable
        @Override
        public BlockData transformData(ITransactionExecutionContext context, BlockData data) {
            setLastOperation(TransactionOperation.TRANSFORM_DATA);
            if (lastOperation == TransactionOperation.TRANSFORM_DATA)
                -- toShiftCounter;
            if (toShiftCounter <= 0)
                return other.transformData(context, data);
            return data;
        }

        @Nullable
        @Override
        public BlockPos transformPos(ITransactionExecutionContext context, BlockPos pos, BlockData data) {
            setLastOperation(TransactionOperation.TRANSFORM_POSITION);
            if (lastOperation == TransactionOperation.TRANSFORM_POSITION)
                -- toShiftCounter;
            if (toShiftCounter <= 0)
                return other.transformPos(context, pos, data);
            return pos;
        }

        @Nullable
        @Override
        public PlacementTarget transformTarget(ITransactionExecutionContext context, PlacementTarget target) {
            setLastOperation(TransactionOperation.TRANSFORM_TARGET);
            if (lastOperation == TransactionOperation.TRANSFORM_TARGET)
                -- toShiftCounter;
            if (toShiftCounter <= 0)
                return other.transformTarget(context, target);
            return target;
        }

        @Override
        public Set<TransactionOperation> remainingOperations() {
            return other.remainingOperations();
        }

        private void setLastOperation(TransactionOperation operation) {
            if (lastOperation == null)
                this.lastOperation = operation;
        }
    }

    /**
     * Creates an {@link ITransactionOperator} which replaces the delegate of an {@link com.direwolf20.buildinggadgets.api.template.DelegatingTemplate}. This doesn't have
     * any effect if the {@link ITemplate} is not an instance of {@link com.direwolf20.buildinggadgets.api.template.DelegatingTemplate} or the implementation doesn't specifically
     * support this Operator!
     */
    public static ReplaceDelegateOperator replaceDelegateOperator(ITemplate newDelegate) {
        return new ReplaceDelegateOperator(Objects.requireNonNull(newDelegate, "Cannot construct a replace Delegate Operator without a new Delegate!"));
    }
}
