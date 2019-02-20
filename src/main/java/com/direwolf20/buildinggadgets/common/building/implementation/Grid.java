package com.direwolf20.buildinggadgets.common.building.implementation;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPlacementSequence;
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

    protected Grid(BlockPos center, int range, int periodSize) {
        this.region = Wall.clickedSide(center, EnumFacing.UP, range).getBoundingBox();
        this.range = range;
        this.center = center;
        this.periodSize = periodSize;
    }

    /**
     * For {@link #copy()}
     */
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

    @Override
    public boolean contains(int x, int y, int z) {
        return false;
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
        //Out of parenthesis: gap + block itself
        int change = ((range - 1) % periodSize + 1) + 1;

        int start = range * -7 / 5;
        int end = range * 7 / 5;

        return new AbstractIterator<BlockPos>() {
            private int x;
            private int z;

            @Override
            protected BlockPos computeNext() {
                if (x >= end && z >= end) {
                    return endOfData();
                }

                x += change;
                if (x > end) {
                    x = start;
                    z += change;
                }

                return new BlockPos(center.getX() + x, center.getY(), center.getZ() + z);
            }
        };
    }

}
