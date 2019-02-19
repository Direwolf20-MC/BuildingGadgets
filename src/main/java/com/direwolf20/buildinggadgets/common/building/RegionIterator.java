package com.direwolf20.buildinggadgets.common.building;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.PeekingIterator;
import net.minecraft.util.math.BlockPos;

class RegionIterator extends AbstractIterator<BlockPos> implements PeekingIterator<BlockPos> {

    //minX does not exist because it is not used here
    private final int minY;
    private final int minZ;

    private final int maxX;
    private final int maxY;
    private final int maxZ;

    private int posX;
    private int posY;
    private int posZ;

    RegionIterator(Region region) {
        this(region.getMinY(), region.getMinZ(), region.getMaxX(), region.getMaxY(), region.getMaxZ());
    }

    private RegionIterator(int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    @Override
    protected BlockPos computeNext() {
        if (this.isTerminated())
            return endOfData();

        if (!isZOverflowed()) {
            posZ++;
        } else if (!isYOverflowed()) {
            posZ = minZ;
            posY++;
        } else if (!isXOverflowed()) {
            posZ = minZ;
            posY = minY;
            posX++;
        }
        return new BlockPos(posX, posY, posZ);
    }

    private boolean isXOverflowed() {
        return posX >= maxX;
    }

    private boolean isYOverflowed() {
        return posY >= maxY;
    }

    private boolean isZOverflowed() {
        return posZ >= maxZ;
    }

    private boolean isTerminated() {
        return isXOverflowed() && isYOverflowed() && isZOverflowed();
    }

}