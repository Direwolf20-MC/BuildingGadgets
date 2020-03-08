package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
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
// 1.14 method
//        Vec3d look = player.getLookVec();
//        Vec3d start = player.getPositionVec().add(0, player.getEyeHeight(), 0);
//
//        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
//        //return world.rayTraceBlocks(start, end, rayTraceFluid, false, false);
//        RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, rayTraceFluid, player);
//        BlockRayTraceResult result = world.rayTraceBlocks(context);
//        //        if (player.world.getBlockData(result.getPos()) == Blocks.AIR.getDefaultState()) return new RayTraceContext.BlockMode.;
//        return result;
    }

    public static BlockPos getPosLookingAt(PlayerEntity player, ItemStack tool) {
        return VectorHelper.getLookingAt(player, tool).getPos();
    }

}
