package com.direwolf20.buildinggadgets.api.building.placement;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.util.MathUtils;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Spliterator;

/**
 * Column is a line of blocks that is aligned to some axis, starting from a position to another where 2 and only 2 coordinates
 * are the same. Whether the resulting {@link BlockPos}es include the start/end position is up to the factory methods'
 * specification.
 */
public final class Column implements IPositionPlacementSequence {

    /**
     * Construct a column object with a starting point, including {@code range} amount of elements.
     *
     * @param hit   the source position, will not be included
     * @param side  side to grow the column into
     * @param range length of the column
     * @implSpec this sequence includes the source position
     */
    public static Column extendFrom(BlockPos hit, Direction side, int range) {
        return new Column(hit, hit.offset(side, range - 1));
    }

    /**
     * Construct a column object of the specified length, centered at a point and aligned to the given axis.
     *
     * @param center center of the column
     * @param axis   which axis will the column align to
     * @param length length of the column, will be floored to an odd number if it is not one already
     */
    public static Column centerAt(BlockPos center, Axis axis, int length) {
        Direction positive = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis);
        Direction negative = positive.getOpposite();
        BlockPos base = center.offset(negative, (length - 1) / 2);
        // -1 because Region's vertexes are inclusive
        return new Column(base, base.offset(positive, MathUtils.floorToOdd(length) - 1));
    }

    private final Region region;

    private Column(BlockPos source, BlockPos target) {
        this.region = new Region(source, target);
    }

    /**
     * For {@link #copy()}
     */
    @VisibleForTesting
    private Column(Region region) {
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

    @Override
    public IPositionPlacementSequence copy() {
        return new Column(region);
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
