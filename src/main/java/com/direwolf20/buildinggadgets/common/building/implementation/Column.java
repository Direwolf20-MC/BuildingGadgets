package com.direwolf20.buildinggadgets.common.building.implementation;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPlacementSequence;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class Column implements IPlacementSequence {

    public static Column extendFrom(EnumFacing side, BlockPos hit, int range) {
        return new Column(hit.offset(side), side, range);
    }

    public static Column centerAt(BlockPos hit, Axis axis, int range) {
        int radius = (range - 1) / 2;
        EnumFacing positive = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, axis);
        BlockPos base = hit.offset(positive.getOpposite(), radius);
        return new Column(base, positive, range);
    }

    private final Region region;

    protected Column(BlockPos base, EnumFacing sideHit, int range) {
        this.region = new Region(base, base.offset(sideHit, range));
    }

    @Override
    public Region getBoundingBox() {
        return region;
    }

    @Override
    @Nonnull
    public Iterator<BlockPos> iterator() {
        return region.iterator();
    }

}
