package com.direwolf20.buildinggadgets.common.items.modes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class GridMode extends AbstractMode {
    public GridMode(boolean isExchanging) {
        super(isExchanging);
    }

    @Override
    List<BlockPos> collect(UseContext context, Player player, BlockPos start) {
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
