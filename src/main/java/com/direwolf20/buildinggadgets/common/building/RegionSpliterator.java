package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

class RegionSpliterator implements Spliterator<BlockPos> {

    private int minX;
    private int minY;
    private int minZ;

    private int maxX;
    private int maxY;
    private int maxZ;

    private int nextPosX;
    private int nextPosY;
    private int nextPosZ;

    /**
     * Note that as soon as this spliterator advanced once, it can no longer be guarantee that the given blocks have not yet been visited
     */
    private boolean allowYZSplit;

    RegionSpliterator(Region region) {
        this(region.getMinX(), region.getMinY(), region.getMinZ(), region.getMaxX(), region.getMaxY(), region.getMaxZ());
    }

    private RegionSpliterator(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this(minX, minY, minZ, maxX, maxY, maxZ, minX, minY, minZ, true);
    }

    private RegionSpliterator(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int posX, int posY, int posZ, boolean allowYZSplit) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        this.nextPosX = posX;
        this.nextPosY = posY;
        this.nextPosZ = posZ;
        this.allowYZSplit = allowYZSplit;
    }

    @Override
    public boolean tryAdvance(Consumer<? super BlockPos> action) {
        if (isXOverflowed()) {
            return false;
        }

        this.allowYZSplit = false;
        BlockPos pos = new BlockPos(nextPosX, nextPosY, nextPosZ);

        nextPosZ++;
        if (isZOverflowed()) {
            nextPosZ = minZ;
            nextPosY++;
        } else {
            action.accept(pos);
            return true;
        }

        if (isYOverflowed()) {
            nextPosY = minY;
            nextPosX++;
        } else {
            action.accept(pos);
            return true;
        }

        //(maxX, maxY, maxZ)
        action.accept(pos);
        return true;
    }

    /**
     * @return A part of this {@link Spliterator}, if splitting is possible
     */
    @Override
    public Spliterator<BlockPos> trySplit() {
        int oldMinX = minX;
        int oldMinY = minY;
        int oldMinZ = minZ;
        int oldPosX = nextPosX;
        int oldPosY = nextPosY;
        int oldPosZ = nextPosZ;

        //Construct new min coordinates, so that at least one can be split of: max - min >= 1
        if (maxX > minX) {
            //As Region's coordinates are inclusive, the amount of blocks along one axis is max - min + 1
            //Half the length + base x
            minX = (maxX - minX + 1) / 2 + minX + 1;
            resetPos();
            return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, minX - 1, maxY, maxZ, oldPosX, oldPosY, oldPosZ, allowYZSplit);
        } else if (maxY > minY && allowYZSplit) {
            minY = (maxY - minY + 1) / 2 + minY + 1;
            resetPos();
            return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, maxX, minY - 1, maxZ, oldPosX, oldPosY, oldPosZ, allowYZSplit);
        } else if (maxZ > minZ && allowYZSplit) {
            minZ = (maxZ - minZ + 1) / 2 + minZ + 1;
            resetPos();
            return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, maxX, maxY, minZ - 1, oldPosX, oldPosY, oldPosZ, allowYZSplit);
        }
        return null;
    }

    @Override
    public long estimateSize() {
        return (long) (Math.abs(maxX - nextPosX) + 1) * (long) (Math.abs(maxY - nextPosY) + 1) * (long) (Math.abs(maxZ - nextPosZ + 1));
    }

    @Override
    public int characteristics() {
        return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SORTED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }

    /**
     * @return {@link GadgetUtils#POSITION_COMPARATOR}
     */
    @Override
    public Comparator<? super BlockPos> getComparator() {
        return GadgetUtils.POSITION_COMPARATOR;
    }

    private boolean isXOverflowed() {
        return nextPosX > maxX;
    }

    private boolean isYOverflowed() {
        return nextPosY > maxY;
    }

    private boolean isZOverflowed() {
        return nextPosZ > maxZ;
    }

    private boolean isTerminated() {
        return isXOverflowed() && isYOverflowed() && isZOverflowed();
    }

    private void resetPos() {
        this.nextPosX = minX;
        this.nextPosY = minY;
        this.nextPosZ = minZ;
    }

}