package com.direwolf20.buildinggadgets.api.building.placement;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.util.MathUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

final class GridSequence implements IPositionPlacementSequence {
    private final int periodSize;
    private final Region region;
    private final BlockPos center;
    private final int range;

    @VisibleForTesting
    GridSequence(BlockPos center, int range, int periodSize) {
        this.region = PlacementSequences.Wall.clickedSide(center, Direction.UP, range).getBoundingBox();
        this.range = range;
        this.center = center;
        this.periodSize = periodSize;
    }

    /**
     * For {@link #copy()}
     */
    @VisibleForTesting
    private GridSequence(Region region, BlockPos center, int range, int periodSize) {
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

    @Override
    public IPositionPlacementSequence copy() {
        return new GridSequence(region, center, range, periodSize);
    }

    @Override
    @Nonnull
    public Iterator<BlockPos> iterator() {
        /* Distance between blocks + block itself
         * arithmetic sequence of [2,7] where -1 for range being 1~15, +2 to shift the sequence from [0,5] to [2,7]
         */
        int period = (range - 1) % periodSize + 2;

        // Random design choice by Dire
        int end = (range + 1) * 7 / 5;
        // Floor to the nearest multiple of period
        int start = MathUtils.floorMultiple(-end, period);

        return new AbstractIterator<BlockPos>() {
            private int x = start;
            private int z = start;

            @Override
            protected BlockPos computeNext() {
                if (z > end)
                    return endOfData();

                BlockPos pos = new BlockPos(center.getX() + x, center.getY(), center.getZ() + z);

                x += period;
                if (x > end) {
                    x = start;
                    z += period;
                }

                return pos;
            }
        };
    }

}
