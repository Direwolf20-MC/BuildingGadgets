package com.direwolf20.buildinggadgets.common.items.gadgets.modes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HorizontalWallMode extends AbstractMode {
    public HorizontalWallMode() { super(false); }

    @Override
    List<BlockPos> collect(PlayerEntity player, BlockPos playerPos, Direction side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // Handle top and bottom first.
        int halfRange = range / 2;
        if( XYZ.isAxisY(side) ) {
            for (int i = -halfRange; i <= halfRange; i ++) {
                for(int j = -halfRange; j <= halfRange; j++)
                    coordinates.add(new BlockPos(start.getX() - i, start.getY(), start.getZ() + j));
            }

            return coordinates;
        }

        // Draw complete column then expand by half the range on both sides :D
        XYZ xyz = XYZ.fromFacing(side);
        for (int i = 0; i < range; i ++) {
            for(int j = -halfRange; j <= halfRange; j++) {
                int value = XYZ.invertOnFace(side, i);
                coordinates.add(
                        xyz == XYZ.X
                                ? new BlockPos(start.getX() + value, start.getY(), start.getZ() + j)
                                : new BlockPos(start.getX() + j, start.getY(), start.getZ() + value)
                );
            }
        }

        return coordinates;
    }
}
