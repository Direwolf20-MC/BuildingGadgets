package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class StairMode extends Mode {
    public StairMode() {
        super("stairs", false);
    }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        Direction side = context.getHitSide();
        if (side.getAxis() == Direction.Axis.Y) {
            side = player.getDirection().getOpposite();
        }

        for (int i = 0; i < context.getRange(); i++) {
            // Check to see if we should build up or down from the player
            int tmp = start.getY() > player.getY() + 1
                ? (i + 1) * -1
                : i;

            if (side.getAxis() == Direction.Axis.X) {
                coordinates.add(new BlockPos(start.getX() + (tmp * (side == Direction.EAST
                    ? -1
                    : 1)), start.getY() + tmp, start.getZ()));
            }

            if (side.getAxis() == Direction.Axis.Z) {
                coordinates.add(new BlockPos(start.getX(), start.getY() + tmp, start.getZ() + (tmp * (side == Direction.SOUTH
                    ? -1
                    : 1))));
            }
        }

        return coordinates;
    }

    @Override
    public BlockPos withOffset(BlockPos pos, Direction side, boolean placeOnTop) {
        // Is top / bottom? Do as normal. Not? then place on top or inside :D
        return side.getAxis() == Direction.Axis.Y
            ? super.withOffset(pos, side, placeOnTop)
            : (placeOnTop
                ? pos.relative(Direction.UP)
                : pos);
    }
}
