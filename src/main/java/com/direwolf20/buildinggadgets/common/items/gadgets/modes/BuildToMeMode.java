package com.direwolf20.buildinggadgets.common.items.gadgets.modes;

import com.direwolf20.buildinggadgets.common.config.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BuildToMeMode extends AbstractMode {
    public BuildToMeMode() { super(false); }

    @Override
    public List<BlockPos> collect(PlayerEntity player, BlockPos playerPos, Direction side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        XYZ facingXYZ = XYZ.fromFacing(side);

        int startCoord = XYZ.posToXYZ(start, facingXYZ);
        int playerCoord = XYZ.posToXYZ(playerPos, facingXYZ);

        // Clamp the value to the max range of the gadgets raytrace
        double difference = Math.max(0, Math.min(Config.GENERAL.rayTraceRange.get(), Math.abs(startCoord - playerCoord)));
        for( int i = 0; i < difference; i ++)
            coordinates.add(XYZ.extendPosSingle(i, start, side, facingXYZ));

        return coordinates;
    }
}
