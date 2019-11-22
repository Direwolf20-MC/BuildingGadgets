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

        start = start.offset(side, -1).offset(EnumFacing.UP);

        XYZ facingXYZ = XYZ.fromFacing(side);

        for( int i = 0; i < range; i ++ ) {
            // Check to see if we should build up or down from the player
            int tmp = start.getY() > player.posY + 1 ? (i + 1) * -1 : i;

            if( facingXYZ == XYZ.X )
                coordinates.add(new BlockPos(start.getX() + (tmp * (side == EnumFacing.EAST ? -1 : 1)), start.getY() + tmp, start.getZ()));

            if( facingXYZ == XYZ.Z )
                coordinates.add(new BlockPos(start.getX(), start.getY() + tmp, start.getZ() + (tmp * (side == EnumFacing.SOUTH ? -1 : 1))));
        }

        return coordinates;
    }

    @Override
    public BlockPos withOffset(BlockPos pos, EnumFacing side, boolean placeOnTop) {
        // Is top / bottom? Do as normal. Not? then place on top or inside :D
        return XYZ.isAxisY(side) ? super.withOffset(pos, side, placeOnTop) : (placeOnTop ? pos.offset(EnumFacing.UP) : pos);
    }
}
