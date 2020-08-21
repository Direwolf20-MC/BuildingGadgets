package com.direwolf20.buildinggadgets.common.items.modes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @implNote I'm 100% sure I could solve the xyz == XYZ.X by using a helper method but I'm happy
 *           with it for now.
 */
public class VerticalWallMode extends AbstractMode {
    public VerticalWallMode() { super(false); }

    @Override
    List<BlockPos> collect(UseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // Handle top and bottom
        int halfRange = context.getRange() / 2;
        if( XYZ.isAxisY(context.getHitSide()) ) {
            // This allows us to figure out how to move the render
            XYZ xyz = XYZ.fromFacing(player.getHorizontalFacing().getOpposite());
            for(int i = 0; i < context.getRange(); i ++ ) {
                for(int j = -halfRange; j <= halfRange; j ++) {
                    int value = XYZ.invertOnFace(context.getHitSide(), i);

                    // Depending on the player view, change the expansion point.
                    coordinates.add(
                            xyz == XYZ.X
                                ? new BlockPos(start.getX(), start.getY() + value, start.getZ() + j)
                                : new BlockPos(start.getX() + j, start.getY() + value, start.getZ())
                    );
                }
            }

            return coordinates;
        }

        // Handle sides. Half and half :D
        XYZ xyz = XYZ.fromFacing(context.getHitSide());
        for (int i = -halfRange; i <= halfRange; i ++) {
            for(int j = -halfRange; j <= halfRange; j++)
                coordinates.add(
                        xyz == XYZ.X
                                ? new BlockPos(start.getX(), start.getY() + i, start.getZ() + j)
                                : new BlockPos(start.getX() + j, start.getY() + i, start.getZ())
                );
        }

        return coordinates;
    }

    /**
     * We need to modify where the offset is for this mode as when looking at any
     * face that isn't up or down, we need to push the offset back into the block
     * and ignore placeOnTop as this mode does the action by default.
     */
    @Override
    public BlockPos withOffset(BlockPos pos, Direction side, boolean placeOnTop) {
        return XYZ.isAxisY(side) ? super.withOffset(pos, side, placeOnTop) : pos;
    }
}
