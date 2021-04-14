package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import com.direwolf20.buildinggadgets.common.construction.XYZ;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HorizontalWallMode extends Mode {
    public HorizontalWallMode() {
        super("horizontal_wall", false);
    }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // Handle top and bottom first.
        int halfRange = context.getRange() / 2;
        if (context.getHitSide().getAxis() == Direction.Axis.Y) {
            for (int i = -halfRange; i <= halfRange; i++) {
                for (int j = -halfRange; j <= halfRange; j++) {
                    coordinates.add(new BlockPos(start.getX() - i, start.getY(), start.getZ() + j));
                }
            }

            return coordinates;
        }

        // Draw complete column then expand by half the range on both sides :D
        for (int i = 0; i < context.getRange(); i++) {
            for (int j = -halfRange; j <= halfRange; j++) {
                int value = XYZ.invertOnFace(context.getHitSide(), i);
                coordinates.add(
                    context.getHitSide().getAxis() == Direction.Axis.X
                        ? new BlockPos(start.getX() + value, start.getY(), start.getZ() + j)
                        : new BlockPos(start.getX() + j, start.getY(), start.getZ() + value)
                );
            }
        }

        return coordinates;
    }
}
