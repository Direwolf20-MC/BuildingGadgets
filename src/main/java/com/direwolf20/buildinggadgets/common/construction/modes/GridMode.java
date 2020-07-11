package com.direwolf20.buildinggadgets.common.modes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GridMode extends Mode {
    public GridMode(boolean isExchanging) {
        super("grid", isExchanging);
    }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // Not sure on why we add 1 to the range but sure?
        int range = context.getRange() + 1;

        for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
            for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                if (x % (((range - 2) % 6) + 2) != 0 || z % (((range - 2) % 6) + 2) != 0)
                    continue;

                coordinates.add(new BlockPos(start.getX() + x, start.getY(), start.getZ() + z));
            }
        }

        return coordinates;
    }
}
