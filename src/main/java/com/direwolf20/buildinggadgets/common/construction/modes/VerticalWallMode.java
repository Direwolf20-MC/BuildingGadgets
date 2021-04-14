package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import com.direwolf20.buildinggadgets.common.construction.XYZ;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * @implNote I'm 100% sure I could solve the xyz == XYZ.X by using a helper method but I'm happy
 * with it for now.
 * @todo clean up
 */
public class VerticalWallMode extends Mode {
    public VerticalWallMode() {
        super("vertical_wall", false);
    }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // Handle top and bottom
        int halfRange = context.getRange() / 2;
        if (context.getHitSide().getAxis() == Direction.Axis.Y) {
            // This allows us to figure out how to move the render
            for (int i = 0; i < context.getRange(); i++) {
                for (int j = -halfRange; j <= halfRange; j++) {
                    int value = XYZ.invertOnFace(context.getHitSide(), i);

                    // Depending on the player view, change the expansion point.
                    coordinates.add(
                        player.getDirection().getOpposite().getAxis() == Direction.Axis.X
                            ? new BlockPos(start.getX(), start.getY() + value, start.getZ() + j)
                            : new BlockPos(start.getX() + j, start.getY() + value, start.getZ())
                    );
                }
            }

            return coordinates;
        }

        // Handle sides. Half and half :D
        for (int i = -halfRange; i <= halfRange; i++) {
            for (int j = -halfRange; j <= halfRange; j++) {
                coordinates.add(
                    context.getHitSide().getAxis() == Direction.Axis.X
                        ? new BlockPos(start.getX(), start.getY() + i, start.getZ() + j)
                        : new BlockPos(start.getX() + j, start.getY() + i, start.getZ())
                );
            }
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
        return side.getAxis() == Direction.Axis.Y
            ? super.withOffset(pos, side, placeOnTop)
            : pos;
    }
}
