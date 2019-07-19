package com.direwolf20.buildinggadgets.api.building.placement;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.util.VectorUtils;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Starts from the selected position, and extend a column of blocks towards a target position on the axis of the selected face.
 */
public final class ExclusiveAxisChasing implements IPositionPlacementSequence {

    public static ExclusiveAxisChasing create(BlockPos source, BlockPos target, Axis axis, int maxProgression) {
        int difference = VectorUtils.getAxisValue(target, axis) - VectorUtils.getAxisValue(source, axis);
        if (difference < 0)
            return create(source, target, Direction.getFacingFromAxis(AxisDirection.NEGATIVE, axis), maxProgression);
        return create(source, target, Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis), maxProgression);
    }

    /**
     * <p>Note that this factory method does not verify that {@code offsetDirection} is appropriate. Use {@link #create(BlockPos, BlockPos, Axis, int)} if this is required.</p>
     */
    public static ExclusiveAxisChasing create(BlockPos source, BlockPos target, Direction offsetDirection, int maxProgression) {
        Axis axis = offsetDirection.getAxis();
        int difference = VectorUtils.getAxisValue(target, axis) - VectorUtils.getAxisValue(source, axis);
        maxProgression = Math.min(Math.abs(difference), maxProgression);

        return new ExclusiveAxisChasing(source, offsetDirection, maxProgression);
    }

    private final BlockPos source;
    private final Direction offsetDirection;
    private final int maxProgression;

    public ExclusiveAxisChasing(BlockPos source, Direction offsetDirection, int maxProgression) {
        this.source = source;
        this.offsetDirection = offsetDirection;
        this.maxProgression = maxProgression;
    }

    @Override
    public Region getBoundingBox() {
        return null;
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        Axis axis = offsetDirection.getAxis();
        int value = VectorUtils.getAxisValue(x, y, z, axis);
        int sourceValue = VectorUtils.getAxisValue(source, axis);
        int difference = Math.abs(value - sourceValue);
        return difference > 0 && difference < maxProgression;
    }

    @Override
    public IPositionPlacementSequence copy() {
        return new ExclusiveAxisChasing(source, offsetDirection, maxProgression);
    }

    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private int progression = 0;

            @Override
            protected BlockPos computeNext() {
                if (progression >= maxProgression)
                    return endOfData();

                return source.offset(offsetDirection, progression++);
            }
        };
    }

}
