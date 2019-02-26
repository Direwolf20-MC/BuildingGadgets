package com.direwolf20.buildinggadgets.common.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static com.direwolf20.buildinggadgets.common.config.SyncedConfig.rayTraceRange;

public class VectorTools {

    public static RayTraceResult getLookingAt(EntityPlayer player) {
        World world = player.world;
        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        //rayTraceRange here refers to SyncedConfig.rayTraceRange
        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
        return world.rayTraceBlocks(start, end, false, false, false);
    }

    @Nullable
    public static BlockPos getPosLookingAt(EntityPlayer player) {
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        if (lookingAt == null) return null;
        BlockPos pos = lookingAt.getBlockPos();
        return pos;
    }

    public static int getAxisValue(BlockPos pos, Axis axis) {
        switch (axis) {
            case X:
                return pos.getX();
            case Y:
                return pos.getY();
            case Z:
                return pos.getZ();
        }
        throw new IllegalArgumentException("Trying to find the value a imaginary axis of a BlockPos");
    }

    public static int getAxisValue(int x, int y, int z, Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        throw new IllegalArgumentException("Trying to find the value a imaginary axis of a set of 3 values");
    }

    public static BlockPos perpendicularSurfaceOffset(BlockPos pos, EnumFacing intersector, int i, int j) {
        return perpendicularSurfaceOffset(pos, intersector.getAxis(), i, j);
    }

    public static BlockPos perpendicularSurfaceOffset(BlockPos pos, Axis intersector, int i, int j) {
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
