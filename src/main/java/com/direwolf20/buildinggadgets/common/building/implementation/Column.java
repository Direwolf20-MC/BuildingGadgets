package com.direwolf20.buildinggadgets.common.building.implementation;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Spliterator;

public final class Column implements IPlacementSequence {

    public static Column extendFrom(BlockPos hit, EnumFacing side, int range) {
        return new Column(hit.offset(side), side, range);
    }

    public static Column centerAt(BlockPos hit, Axis axis, int range) {
        int radius = (range - 1) / 2;
        EnumFacing positive = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis);
        BlockPos base = hit.offset(positive.getOpposite(), radius);
        return new Column(base, positive, range);
    }

    /**
     * A placement sequence from {@code BlockPos hit} position to the entity's position on the given axis (parameter {@code EnumFacing sideHit})
     * @implNote will not place a block at entity's position
     */
    public static Column chaseAxisToEntity(Entity entity, BlockPos hit, EnumFacing sideHit) {
        BlockPos entityPos = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));
        Axis axis = sideHit.getAxis();
        //Don't +1 to prevent placing block at entity's position
        int limit = Math.abs(VectorTools.getAxisValue(entityPos, axis) - VectorTools.getAxisValue(hit, axis));
        return new Column(hit.offset(sideHit), sideHit, limit);
    }

    private final Region region;

    protected Column(BlockPos base, EnumFacing sideHit, int range) {
        this.region = new Region(base, base.offset(sideHit, range));
    }

    /**
     * For {@link #copy()}
     */
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
