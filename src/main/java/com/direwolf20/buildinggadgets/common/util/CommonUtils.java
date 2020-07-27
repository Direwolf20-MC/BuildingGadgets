package com.direwolf20.buildinggadgets.common.util;

import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;

public final class CommonUtils {
    private CommonUtils() {}

    public static BlockRayTraceResult fakeRayTrace(Vector3d simulatePos, BlockPos pos) {
        Vector3d simVec = Vector3d.of(pos).subtract(simulatePos);
        Direction dir = Direction.getFacingFromVector(simVec.getX(), simVec.getY(), simVec.getZ());
        return new BlockRayTraceResult(simVec, dir, pos, false);
    }

    public static MaterialList estimateRequiredItems(Iterable<PlacementTarget> buildView, IBuildContext context, @Nullable Vector3d simulatePos) {
        MaterialList.SubEntryBuilder builder = MaterialList.andBuilder();
        for (PlacementTarget placementTarget : buildView) {
            BlockRayTraceResult target = simulatePos != null ? CommonUtils.fakeRayTrace(simulatePos, placementTarget.getPos()) : null;
            builder.add(placementTarget.getRequiredMaterials(context, target));
        }
        return builder.build();
    }
}
