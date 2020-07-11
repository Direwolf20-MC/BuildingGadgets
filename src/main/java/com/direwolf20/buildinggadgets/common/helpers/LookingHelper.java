package com.direwolf20.buildinggadgets.common.helpers;

import com.direwolf20.buildinggadgets.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceContext.FluidMode;

import javax.annotation.Nullable;

public class LookingHelper {
    public static RayTraceResult getResult(PlayerEntity player, boolean traceFluid) {
        double range = Config.COMMON_CONFIG.gadgetRayTraceRange.get();

        return player.world.rayTraceBlocks(new RayTraceContext(
                player.getEyePosition(1.0f),                                                    // start pos
                player.getEyePosition(1.0f).add(player.getLookVec().mul(range, range, range)),  // end pos
                RayTraceContext.BlockMode.OUTLINE,
                traceFluid ? FluidMode.ANY : FluidMode.NONE,
                player
        ));
    }

    public static Vec3d getPos(PlayerEntity player, boolean traceFluid) {
        return getResult(player, traceFluid).getHitVec();
    }

    public static BlockPos getBlockPos(PlayerEntity player, boolean traceFluid) {
        Vec3d vec = getResult(player, traceFluid).getHitVec();
        return new BlockPos((int) vec.x, (int) vec.y, (int) vec.y);
    }

    @Nullable
    public static BlockRayTraceResult getBlockResult(PlayerEntity player, boolean traceFluid) {
        RayTraceResult result = getResult(player, traceFluid);
        if( !result.getType().equals(RayTraceResult.Type.BLOCK) ) {
            return null;
        }

        return (BlockRayTraceResult) result;
    }
}
