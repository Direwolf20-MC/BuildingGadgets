package com.direwolf20.buildinggadgets.common.gadgets.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class StairMode extends AbstractMode {
    public StairMode() { super(false); }

    @Override
    List<BlockPos> collect(EntityPlayer player, BlockPos playerPos, EnumFacing side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        if (side == EnumFacing.UP || side == EnumFacing.DOWN)
            side = player.getHorizontalFacing().getOpposite();
        else
            start = start.offset(side, -1).offset(EnumFacing.UP);

        XYZ facingXYZ = XYZ.fromFacing(side);

        for( int i = 0; i < range; i ++ ) {
            int tmp = start.getY() > player.posY + 1 ? (i + 1) * -1 : i;

            if( facingXYZ == XYZ.X )
                coordinates.add(new BlockPos(start.getX() + (tmp * (side == EnumFacing.EAST ? -1 : 1)), start.getY() + tmp, start.getZ()));

            if( facingXYZ == XYZ.Z )
                coordinates.add(new BlockPos(start.getX(), start.getY() + tmp, start.getZ() + (tmp * (side == EnumFacing.SOUTH ? -1 : 1))));
        }

        return coordinates;
    }
}
