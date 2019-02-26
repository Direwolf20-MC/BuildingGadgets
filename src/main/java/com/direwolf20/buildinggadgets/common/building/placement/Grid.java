package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

public final class Grid implements IPlacementSequence {

    public static Grid create(BlockPos base, int range, int periodSize) {
        return new Grid(base, range, periodSize);
    }

    private final int periodSize;
    private final Region region;
    private final BlockPos center;
    private final int range;

    @VisibleForTesting
    private Grid(BlockPos center, int range, int periodSize) {
        this.region = Wall.clickedSide(center, EnumFacing.UP, range).getBoundingBox();
        this.range = range;
        this.center = center;
        this.periodSize = periodSize;
    }

    /**
     * For {@link #copy()}
     */
    @VisibleForTesting
    private Grid(Region region, BlockPos center, int range, int periodSize) {
        this.region = region;
        this.center = center;
        this.range = range;
        this.periodSize = periodSize;
    }

    @Override
    public Region getBoundingBox() {
        return region;
    }

    /**
     * {@inheritDoc}<br>
     * <b>inaccurate representation (case 2)</b>:
     */
    @Override
    public boolean mayContain(int x, int y, int z) {
        return region.contains(x, y, z);
    }

    /**
     * @deprecated Grid should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new Grid(region, center, range, periodSize);
    }

    @Override
    @Nonnull
    public Iterator<BlockPos> iterator() {
        //In parenthesis: arithmetic sequence to periodic sequence (-1 to shift the sequence to start at 0, +1 to shift the sequence back to staring at 1)
        int distance = (range - 1) % periodSize + 1;
        //Distance between blocks + block itself
        int change = distance + 1;

        //TODO better size calculation that isn't funky
//        //-1 for periodSize being inclusive
//        int period = (range - 1) % periodSize;

        int start = -range - 1;
        int end = range + 1;

        return new AbstractIterator<BlockPos>() {
            private int x = start;
            private int z = start;

            @Override
            protected BlockPos computeNext() {
                if (z > end) {
                    return endOfData();
                }

                BlockPos pos = new BlockPos(center.getX() + x, center.getY(), center.getZ() + z);

                x += change;
                if (x > end) {
                    x = start;
                    z += change;
                }

                return pos;
            }
        };
    }

}
