package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
//import net.minecraft.util.math.RayTraceFluidMode;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class VectorHelper {

    public static RayTraceResult getLookingAt(PlayerEntity player, ItemStack tool) {
        return getLookingAt(player, GadgetGeneric.shouldRayTraceFluid(tool) ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE);
    }

    public static RayTraceResult getLookingAt(PlayerEntity player, RayTraceContext.FluidMode rayTraceFluid) {
        World world = player.world;

        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        double rayTraceRange = Config.GENERAL.rayTraceRange.get();
        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
        //return world.rayTraceBlocks(start, end, rayTraceFluid, false, false);
        RayTraceContext context = new RayTraceContext(start,end,RayTraceContext.BlockMode.COLLIDER,rayTraceFluid, player);
        RayTraceResult result = world.func_217299_a(context);
        return result;
    }

    @Nullable
    public static BlockPos getPosLookingAt(ClientPlayerEntity player, ItemStack tool) {
        RayTraceResult lookingAt = VectorHelper.getLookingAt(player, tool);
        if (lookingAt == null)
            return null;

        return new BlockPos(lookingAt.getHitVec().x,lookingAt.getHitVec().y,lookingAt.getHitVec().z);
    }

}
