package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * @see Column#inclusiveAxisChasing(BlockPos, BlockPos, EnumFacing)
 */
public final class ExclusiveAxisChasing implements IPlacementSequence {

    public static ExclusiveAxisChasing create(BlockPos source, BlockPos target, Axis axis) {
        int difference = VectorTools.getAxisValue(target, axis) - VectorTools.getAxisValue(source, axis);
        if (difference < 0)
            return create(source, target, EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis));
        return create(source, target, EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axis));
    }

    /**
     * <p>Note that this factory method does not ensure {@code offsetDirection} is appropriate. Use {@link #create(BlockPos, BlockPos, Axis)} if this is required.</p>
     */
    public static ExclusiveAxisChasing create(BlockPos source, BlockPos target, EnumFacing offsetDirection) {
        Axis axis = offsetDirection.getAxis();
        int difference = VectorTools.getAxisValue(target, axis) - VectorTools.getAxisValue(source, axis);
        int maxProgression = MathHelper.clamp(Math.abs(difference), 1, Integer.MAX_VALUE);

        return new ExclusiveAxisChasing(source, offsetDirection, maxProgression);
    }

    private final BlockPos source;
    private final EnumFacing offsetDirection;
    private final int maxProgression;

    public ExclusiveAxisChasing(BlockPos source, EnumFacing offsetDirection, int maxProgression) {
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
        int value = VectorTools.getAxisValue(x, y, z, axis);
        int sourceValue = VectorTools.getAxisValue(source, axis);
        int difference = Math.abs(value - sourceValue);
        return difference > 0 && difference < maxProgression;
    }

    /**
     * @deprecated ExclusiveAxisChasing should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new ExclusiveAxisChasing(source, offsetDirection, maxProgression);
    }

    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private int progression = 1;

            @Override
            protected BlockPos computeNext() {
                if (progression >= maxProgression)
                    return endOfData();

                return source.offset(offsetDirection, progression++);
            }
        };
    }

}
