package com.direwolf20.buildinggadgets.common.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static com.direwolf20.buildinggadgets.common.config.SyncedConfig.rayTraceRange;

public class RayTraceHelper {

    public static RayTraceResult rayTrace(EntityPlayer player, boolean stopAtFluid) {
        Pair<Vec3d, Vec3d> vectors = getVectors(player);

        return player.world.rayTraceBlocks(
                vectors.getLeft(),
                vectors.getRight(),
                stopAtFluid,
                false,
                false
        );
    }

    private static Pair<Vec3d, Vec3d> getVectors(EntityPlayer player) {
        Vec3d eyePos = player.getPositionEyes(1.0f); // Sending 1.0f returns a pre-calculated version
        Vec3d lookVec = player.getLookVec();

        return new ImmutablePair<>(eyePos, eyePos.addVector(lookVec.x * rayTraceRange, lookVec.y * rayTraceRange, lookVec.z * rayTraceRange));
    }
}
