package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceFluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class VectorHelper {

    public static RayTraceResult getLookingAt(EntityPlayer player, ItemStack tool) {
        return getLookingAt(player, GadgetGeneric.shouldRayTraceFluid(tool) ? RayTraceFluidMode.ALWAYS : RayTraceFluidMode.NEVER);
    }

    public static RayTraceResult getLookingAt(EntityPlayer player, RayTraceFluidMode rayTraceFluid) {
        World world = player.world;

        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        double rayTraceRange = Config.GENERAL.rayTraceRange.get();
        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
        return world.rayTraceBlocks(start, end, rayTraceFluid, false, false);
    }

    @Nullable
    public static BlockPos getPosLookingAt(EntityPlayer player, ItemStack tool) {
        RayTraceResult lookingAt = VectorHelper.getLookingAt(player, tool);
        if (lookingAt == null)
            return null;

        return lookingAt.getBlockPos();
    }

    public static int getAxisValue(BlockPos pos, EnumFacing.Axis axis) {
        switch (axis) {
            case X:
                return pos.getX();
            case Y:
                return pos.getY();
            case Z:
                return pos.getZ();
        }
        throw new IllegalArgumentException("Trying to find the value an imaginary axis of a BlockPos");
    }

    public static int getAxisValue(int x, int y, int z, EnumFacing.Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        throw new IllegalArgumentException("Trying to find the value an imaginary axis of a set of 3 values");
    }

    public static BlockPos perpendicularSurfaceOffset(BlockPos pos, EnumFacing intersector, int i, int j) {
        return perpendicularSurfaceOffset(pos, intersector.getAxis(), i, j);
    }

    public static BlockPos perpendicularSurfaceOffset(BlockPos pos, EnumFacing.Axis intersector, int i, int j) {
        switch (intersector) {
            case X:
                return pos.add(0, i, j);
            case Y:
                return pos.add(i, 0, j);
            case Z:
                return pos.add(i, j, 0);
        }
        throw new IllegalArgumentException("Unknown facing " + intersector);
    }
}
