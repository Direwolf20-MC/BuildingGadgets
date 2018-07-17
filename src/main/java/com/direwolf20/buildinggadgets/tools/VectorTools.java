package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.config.InGameConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VectorTools {


    public static RayTraceResult getLookingAt(EntityPlayer player) {
        World world = player.world;
        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d end = new Vec3d(player.posX + look.x * InGameConfig.rayTraceRange, player.posY + player.getEyeHeight() + look.y * InGameConfig.rayTraceRange, player.posZ + look.z * InGameConfig.rayTraceRange);
        return world.rayTraceBlocks(start, end, false, false, false);
    }
}
