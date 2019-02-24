package com.direwolf20.buildinggadgets.common.utils.helpers;

import com.direwolf20.buildinggadgets.common.config.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceFluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class VectorHelper {

    public static RayTraceResult getLookingAt(EntityPlayer player) {
        World world = player.world;

        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        double rayTraceRange = Config.GENERAL.rayTraceRange.get();
        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
        return world.rayTraceBlocks(start, end, RayTraceFluidMode.NEVER, false, false);
    }

    //for more options in raytracing
    public static RayTraceResult getLookingAt(EntityPlayer player, RayTraceFluidMode fMode) {
        World world = player.world;

        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        double rayTraceRange = Config.GENERAL.rayTraceRange.get();
        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
        return world.rayTraceBlocks(start, end, fMode, false, false);
    }

    @Nullable
    public static BlockPos getPosLookingAt(EntityPlayer player) {
        RayTraceResult lookingAt = VectorHelper.getLookingAt(player);
        if (lookingAt == null)
            return null;

        return lookingAt.getBlockPos();
    }

    //again, for more options
    public static BlockPos getPosLookingAt(EntityPlayer player, RayTraceFluidMode fMode) {
        RayTraceResult lookingAt = VectorUtil.getLookingAt(player, fMode);
        if (lookingAt == null)
            return null;

        return lookingAt.getBlockPos();
    }
}
