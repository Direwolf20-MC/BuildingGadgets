package com.direwolf20.buildinggadgets.building.placement;

import com.direwolf20.buildinggadgets.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.building.Region;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Spliterator;

public final class Column implements IPlacementSequence {

    /**
     * Construct a column object such that it will include {@code range} amount of elements.
     *
     * @implSpec this sequence does <b>not</b> include the source position
     */
    public static Column extendFrom(BlockPos hit, EnumFacing side, int range) {
        //-1 Region's vertexes are inclusive
        return new Column(hit.offset(side), hit.offset(side, range));
    }

    public static Column centerAt(BlockPos hit, Axis axis, int range) {
        int radius = (range - 1) / 2;
        EnumFacing positive = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis);
        BlockPos base = hit.offset(positive.getOpposite(), radius);
        //-1 because Region's vertexes are inclusive
        return new Column(base, base.offset(positive, range - 1));
    }

    /**
     * A placement sequence from {@code BlockPos hit} position to the target position on the given axis (parameter {@code EnumFacing sideHit}).
     *
     * @implSpec will not place a block at target position, but will place a block at source position
     */
    public static Column chaseAxisTo(BlockPos pos, BlockPos source, EnumFacing side) {
        Axis axis = side.getAxis();
        //Don't +1 to prevent placing block at target position
        int size = Math.abs(VectorTools.getAxisValue(pos, axis) - VectorTools.getAxisValue(source, axis));
        //-1 because Region's vertexes are inclusive
        return new Column(source, source.offset(side, size - 1));
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
    public boolean contains(int x, int y, int z) {
        return region.contains(x, y, z);
    }

    /**
     * @deprecated Column should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
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
