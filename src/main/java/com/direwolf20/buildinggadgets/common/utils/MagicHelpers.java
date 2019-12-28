package com.direwolf20.buildinggadgets.common.utils;

import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.direwolf20.buildinggadgets.common.config.SyncedConfig.rayTraceRange;

public class MagicHelpers {

    /**
     * @param list an unsorted {@link BlockPos} Collection
     * @param player the player
     * @return a sorted by distance {@link List} of {@link BlockPos}
     */
    public static List<BlockPos> byDistance(Collection<BlockPos> list, EntityPlayer player) {
        List<BlockPos> sortedList = new ArrayList<>();

        Double2ObjectMap<BlockPos> rangeMap = new Double2ObjectArrayMap<>(list.size());
        DoubleSortedSet distances = new DoubleRBTreeSet();

        double  x = player.posX,
                y = player.posY + player.getEyeHeight(),
                z = player.posZ;

        list.forEach(pos -> {
            double distance = pos.distanceSqToCenter(x, y, z);

            rangeMap.put(distance, pos);
            distances.add(distance);
        } );

        for (double dist : distances) {
            sortedList.add(rangeMap.get(dist));
        }

        return sortedList;
    }

    @Nullable
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
