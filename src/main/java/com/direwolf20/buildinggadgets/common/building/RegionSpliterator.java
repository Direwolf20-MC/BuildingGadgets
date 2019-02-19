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

    private int posX;
    private int posY;
    private int posZ;

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
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.allowYZSplit = allowYZSplit;
    }

    @Override
    public boolean tryAdvance(Consumer<? super BlockPos> action) {
        if (this.isTerminated())
            return false;

        this.allowYZSplit = false;
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

        action.accept(new BlockPos(posX, posY, posZ));
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

        //construct new min coordinates, so that at least one can be split of: max - min >= 1
        if (maxX > minX) { //as Region's coordinates are inclusive, the amount of blocks along one axis is max-min+1
            minX = (maxX - minX + 1) / 2 + minX + 1;
            resetPos();
            return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, minX - 1, maxY, maxZ, posX, posY, posZ, allowYZSplit);
        } else if (maxY > minY && allowYZSplit) {
            minY = (maxY - minY + 1) / 2 + minY + 1;
            resetPos();
            return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, maxX, minY - 1, maxZ, posX, posY, posZ, allowYZSplit);
        } else if (maxZ > minZ && allowYZSplit) {
            minZ = (maxZ - minZ + 1) / 2 + minZ + 1;
            resetPos();
            return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, maxX, maxY, minZ - 1, posX, posY, posZ, allowYZSplit);
        }
        return null;
    }

    @Override
    public long estimateSize() {
        return (long) (maxX - posX) * (long) (maxY - posY) * (long) (maxZ - posZ);
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

    private void resetPos() {
        this.posX = minX;
        this.posY = minY;
        this.posZ = minZ;
    }

}