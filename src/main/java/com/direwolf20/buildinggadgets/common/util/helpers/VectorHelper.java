package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceFluidMode;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class VectorHelper {

    public static RayTraceResult getLookingAt(ClientPlayerEntity player, ItemStack tool) {
        return getLookingAt(player, GadgetGeneric.shouldRayTraceFluid(tool) ? RayTraceContext.FluidMode. : RayTraceContext.FluidMode.NONE);
    }

    public static RayTraceResult getLookingAt(ClientPlayerEntity player, RayTraceFluidMode rayTraceFluid) {
        World world = player.world;

        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        double rayTraceRange = Config.GENERAL.rayTraceRange.get();
        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
        return world.rayTraceBlocks(start, end, rayTraceFluid, false, false);
    }

    @Nullable
    public static BlockPos getPosLookingAt(ClientPlayerEntity player, ItemStack tool) {
        RayTraceResult lookingAt = VectorHelper.getLookingAt(player, tool);
        if (lookingAt == null)
            return null;

        return lookingAt.getBlockPos();
    }

}
