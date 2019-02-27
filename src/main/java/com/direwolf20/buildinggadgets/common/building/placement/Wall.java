package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Spliterator;

public final class Wall implements IPlacementSequence {

    /**
     * @param center the center of the wall
     * @param side   the side to be become a flat surface
     * @param range  length of the wall, which is floored to the nearest odd number
     */
    public static Wall clickedSide(BlockPos center, EnumFacing side, int range) {
        return new Wall(center, side, toRadius(range));
    }

    public static Wall extendingFrom(BlockPos posHit, EnumFacing extension, EnumFacing flatSide, int range) {
        Preconditions.checkArgument(extension != flatSide, "Cannot have a wall extending to " + extension + " and flat at " + flatSide);

        int radius = toRadius(range);
        return new Wall(posHit.offset(extension, radius + 1), flatSide, radius);
    }

    private static int toRadius(int range) {
        return (range - 1) / 2;
    }

    private final Region region;

    @VisibleForTesting
    private Wall(BlockPos posHit, EnumFacing side, int radius) {
        this.region = new Region(posHit).expand(
                radius * (1 - Math.abs(side.getFrontOffsetX())),
                radius * (1 - Math.abs(side.getFrontOffsetY())),
                radius * (1 - Math.abs(side.getFrontOffsetZ())));
    }

    /**
     * For {@link #copy()}
     */
    @VisibleForTesting
    private Wall(Region region) {
        this.region = region;
    }

    @Override
    public Region getBoundingBox() {
        return region;
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return region.mayContain(x, y, z);
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
