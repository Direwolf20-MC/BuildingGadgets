package com.direwolf20.buildinggadgets.common.items.modes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * This implementation is pretty complicate due the convenience settings for placement. We do a relative simple math
 * equation to start with, stairs are simple, take the look vector, then in a single direction x + 1, then increase the height.
 * <p>
 * It gets complicated though as we need to support the following options
 * - If the player is higher than the target block we should build the stairs in reverse up to the player
 * - If the player is looking at the block one under their feet, and we're on a horizontal facing plane, we should build
 *   outwards away from the facing plain.
 * - If we're looking at the top or bottom of a block, we correct the positioning to build as if we were looking at a face
 *   of a block that wasn't the top or bottom.
 */
public class StairMode extends AbstractMode {
    public StairMode() { super(false); }

    @Override
    List<BlockPos> collect(UseContext context, Player player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        Direction side = context.getHitSide();
        if (XYZ.isAxisY(side))
            side = player.getDirection().getOpposite();

        for( int i = 0; i < context.getRange(); i ++ ) {
            var hitSide = context.getHitSide();
            int shiftAxis = (i + 1) * (hitSide == Direction.EAST || hitSide == Direction.SOUTH ? 1 : -1);

            // Special case for looking at block under your feet and looking at the horizontal axis
            if (context.getStartPos().getY() < player.getY() && hitSide.getAxis().isHorizontal()) {
                boolean mutateXAxis = hitSide.getAxis() == Direction.Axis.X;
                boolean mutateZAxis = hitSide.getAxis() == Direction.Axis.Z;

                coordinates.add(context.getStartPos().offset(
                        mutateXAxis ? shiftAxis : 0, // If we're hitting at the X axis we should shift it
                        context.isPlaceOnTop() ? -i : -(i + 1), // If place on top is on, we should place aside the block, otherwise, in the current one
                        mutateZAxis ? shiftAxis : 0 // If we're hitting at the Z axis we should shift the Z axis of the block.
                ));

                continue;
            }

            shiftAxis = i * (side == Direction.EAST || side == Direction.SOUTH ? -1 : 1);
            if (start.getY() < player.getY() - 2) {
                shiftAxis = shiftAxis * -1;
            }

            coordinates.add(start.offset(
                    side.getAxis() == Direction.Axis.X ? shiftAxis : 0,
                    start.getY() > (player.getY() + 1) ? i * -1 : i, // Check to see if we should build up or down from the player
                    side.getAxis() == Direction.Axis.Z ? shiftAxis : 0
            ));
        }

        return coordinates;
    }

    @Override
    public BlockPos withOffset(UseContext context) {
        var side = context.getHitSide();
        var placeOnTop = context.isPlaceOnTop();
        var pos = context.getStartPos();

        // Is top / bottom? Do as normal. Not? then place on top or inside :D
        if (side == Direction.DOWN) {
            return placeOnTop ? pos.relative(side, 1) : pos;
        }

        return XYZ.isAxisY(side) ? super.withOffset(context) : (placeOnTop ? pos.relative(Direction.UP) : pos);
    }
}
