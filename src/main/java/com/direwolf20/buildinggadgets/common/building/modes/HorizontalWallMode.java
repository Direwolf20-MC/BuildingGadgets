package com.direwolf20.buildinggadgets.common.building.modes;

import com.direwolf20.buildinggadgets.common.building.AbstractMode;
import com.direwolf20.buildinggadgets.common.building.BuildingContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HorizontalWallMode extends AbstractMode {
    public HorizontalWallMode() { super(false); }

    @Override
    public List<BlockPos> collect(BuildingContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // Handle top and bottom first.
        int halfRange = context.getRange() / 2;
        if( XYZ.isAxisY(context.getHitSide()) ) {
            for (int i = -halfRange; i <= halfRange; i ++) {
                for(int j = -halfRange; j <= halfRange; j++)
                    coordinates.add(new BlockPos(start.getX() - i, start.getY(), start.getZ() + j));
            }

            return coordinates;
        }

        // Draw complete column then expand by half the range on both sides :D
        XYZ xyz = XYZ.fromFacing(context.getHitSide());
        for (int i = 0; i < context.getRange(); i ++) {
            for(int j = -halfRange; j <= halfRange; j++) {
                int value = XYZ.invertOnFace(context.getHitSide(), i);
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
