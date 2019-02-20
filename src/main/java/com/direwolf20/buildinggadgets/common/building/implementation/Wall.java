package com.direwolf20.buildinggadgets.common.building.implementation;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPlacementSequence;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Spliterator;

public final class Wall implements IPlacementSequence {

    public static Wall clickedSide(BlockPos posHit, EnumFacing sideHit, int range) {
        return new Wall(posHit, sideHit, toRadius(range));
    }

    public static Wall extendingSide(BlockPos posHit, EnumFacing sideHit, EnumFacing top, int range) {
        int radius = toRadius(range);
        return new Wall(posHit.offset(sideHit, radius + 1), top, radius);
    }

    private static int toRadius(int range) {
        return (range - 1) / 2;
    }

    private final Region region;

    /**
     * @param posHit the center of the wall
     * @param side the side to be become a flat surface
     * @param radius radius of the wall
     */
    protected Wall(BlockPos posHit, EnumFacing side, int radius) {
        this.region = new Region(posHit).grow(
                radius * (1 - Math.abs(side.getFrontOffsetX())),
                radius * (1 - Math.abs(side.getFrontOffsetY())),
                radius * (1 - Math.abs(side.getFrontOffsetZ())));
    }

    /**
     * For {@link #copy()}
     */
    private Wall(Region region) {
        this.region = region;
    }

    @Override
    public Region getBoundingBox() {
        return region;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return region.contains(x, y, z);
    }

    /**
     * @deprecated Wall should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new Wall(region);
    }

    @Override
    @Nonnull
    public Iterator<BlockPos> iterator() {
        return region.iterator();
    }

    @Override
    public Spliterator<BlockPos> spliterator() {
        return region.spliterator();
    }

}
