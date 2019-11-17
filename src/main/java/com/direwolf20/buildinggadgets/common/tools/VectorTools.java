package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.gadgets.GadgetGeneric;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static com.direwolf20.buildinggadgets.common.config.SyncedConfig.rayTraceRange;

public class VectorTools {

    public static RayTraceResult getLookingAt(EntityPlayer player, ItemStack tool) {
        return getLookingAt(player, GadgetGeneric.shouldRayTraceFluid(tool));
    }

    public static RayTraceResult getLookingAt(EntityPlayer player, boolean rayTraceFluid) {
        World world = player.world;
        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        //rayTraceRange here refers to SyncedConfig.rayTraceRange
        Vec3d end = new Vec3d(player.posX + look.x * rayTraceRange, player.posY + player.getEyeHeight() + look.y * rayTraceRange, player.posZ + look.z * rayTraceRange);
        return world.rayTraceBlocks(start, end, rayTraceFluid, false, false);
    }

    @Nullable
    public static BlockPos getPosLookingAt(EntityPlayer player, ItemStack tool) {
        RayTraceResult lookingAt = VectorTools.getLookingAt(player, tool);
        if (lookingAt == null)
            return null;

        return lookingAt.getBlockPos();
    }
}
