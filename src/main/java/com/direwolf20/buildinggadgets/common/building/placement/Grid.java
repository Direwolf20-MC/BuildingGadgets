package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.tools.MathTool;
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
        //Distance between blocks + block itself
        //Arithmetic sequence of [2,7]
        //-1 for range being 1~15, +2 to shift the sequence from [0,6] to [2,7]
        int period = (range - 1) % periodSize + 2;

        //Random design choice by Dire
        int end = (range + 1) * 7 / 5;
        //Floor to the nearest multiple of period
        int start = MathTool.floorMultiple(-end, period);

        return new AbstractIterator<BlockPos>() {
            private int x = start;
            private int z = start;

            @Override
            protected BlockPos computeNext() {
                if (z > end) {
                    return endOfData();
                }

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
