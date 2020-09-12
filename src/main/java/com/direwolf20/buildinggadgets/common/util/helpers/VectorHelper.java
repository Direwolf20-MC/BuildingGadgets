package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;

public class VectorHelper {

    public static BlockRayTraceResult getLookingAt(PlayerEntity player, ItemStack tool) {
        return getLookingAt(player, AbstractGadget.shouldRayTraceFluid(tool) ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
    }

    public static BlockRayTraceResult getLookingAt(PlayerEntity player, boolean shouldRayTrace) {
        return getLookingAt(player, shouldRayTrace ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
    }

    public static BlockRayTraceResult getLookingAt(PlayerEntity player, RayTraceContext.FluidMode rayTraceFluid) {
        double rayTraceRange = Config.GENERAL.rayTraceRange.get();
        RayTraceResult result = player.pick(rayTraceRange, 0f, rayTraceFluid != RayTraceContext.FluidMode.NONE);

        return (BlockRayTraceResult) result;
    }

}
