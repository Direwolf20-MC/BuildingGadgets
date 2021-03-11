package com.direwolf20.buildinggadgets.common.items.modes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class StairMode extends AbstractMode {
    public StairMode() { super(false); }

    @Override
    List<BlockPos> collect(UseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        Direction side = context.getHitSide();
        if (XYZ.isAxisY(side))
            side = player.getDirection().getOpposite();

        XYZ facingXYZ = XYZ.fromFacing(side);
        for( int i = 0; i < context.getRange(); i ++ ) {
            // Check to see if we should build up or down from the player
            int tmp = start.getY() > player.getY() + 1 ? (i + 1) * -1 : i;

            if( facingXYZ == XYZ.X )
                coordinates.add(new BlockPos(start.getX() + (tmp * (side == Direction.EAST ? -1 : 1)), start.getY() + tmp, start.getZ()));

            if( facingXYZ == XYZ.Z )
                coordinates.add(new BlockPos(start.getX(), start.getY() + tmp, start.getZ() + (tmp * (side == Direction.SOUTH ? -1 : 1))));
        }

        return coordinates;
    }

    @Override
    public BlockPos withOffset(BlockPos pos, Direction side, boolean placeOnTop) {
        // Is top / bottom? Do as normal. Not? then place on top or inside :D
        return XYZ.isAxisY(side) ? super.withOffset(pos, side, placeOnTop) : (placeOnTop ? pos.relative(Direction.UP) : pos);
    }
}
