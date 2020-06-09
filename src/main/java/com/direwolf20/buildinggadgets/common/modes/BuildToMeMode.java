package com.direwolf20.buildinggadgets.common.modes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BuildToMeMode extends Mode {
    public BuildToMeMode() { super("build_to_me",false); }

    @Override
    public List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        XYZ facingXYZ = XYZ.fromFacing(context.getHitSide());

        int startCoord = XYZ.posToXYZ(start, facingXYZ);
        int playerCoord = XYZ.posToXYZ(player.getPosition(), facingXYZ);

        // Clamp the value to the max range of the gadgets raytrace
        // @todo: put back -> double difference = Math.max(0, Math.min(Config.GENERAL.rayTraceRange.get(), Math.abs(startCoord - playerCoord)));
        double difference = Math.max(0, Math.min(20, Math.abs(startCoord - playerCoord)));
        for( int i = 0; i < difference; i ++)
            coordinates.add(XYZ.extendPosSingle(i, start, context.getHitSide(), facingXYZ));

        return coordinates;
    }
}
