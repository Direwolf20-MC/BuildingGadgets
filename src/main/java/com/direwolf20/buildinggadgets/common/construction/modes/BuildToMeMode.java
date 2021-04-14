package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BuildToMeMode extends Mode {
    public BuildToMeMode() { super("build_to_me",false); }

    @Override
    public List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        int startCoord = context.getHitSide().getAxis().choose(start.getX(), start.getY(), start.getZ());
        int playerCoord = context.getHitSide().getAxis().choose(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ());

        // Clamp the value to the max range of the gadgets raytrace
        double difference = Math.max(0, Math.min(Config.COMMON_CONFIG.gadgetRayTraceRange.get(), Math.abs(startCoord - playerCoord)));
        for( int i = 0; i < difference; i ++) {
            coordinates.add(start.relative(context.getHitSide(), i));
        }

        return coordinates;
    }
}
