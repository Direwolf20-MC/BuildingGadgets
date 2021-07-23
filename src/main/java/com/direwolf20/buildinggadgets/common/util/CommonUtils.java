package com.direwolf20.buildinggadgets.common.util;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.direwolf20.buildinggadgets.common.tainted.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class CommonUtils {
    private CommonUtils() {}

    public static BlockHitResult fakeRayTrace(Vec3 simulatePos, BlockPos pos) {
        Vec3 simVec = Vec3.atLowerCornerOf(pos).subtract(simulatePos);
        Direction dir = Direction.getNearest(simVec.x(), simVec.y(), simVec.z());
        return new BlockHitResult(simVec, dir, pos, false);
    }

    @Tainted(reason = "Part of the material list system")
    public static MaterialList estimateRequiredItems(Iterable<PlacementTarget> buildView, BuildContext context, @Nullable Vec3 simulatePos) {
        MaterialList.SubEntryBuilder builder = MaterialList.andBuilder();
        for (PlacementTarget placementTarget : buildView) {
            BlockHitResult target = simulatePos != null ? CommonUtils.fakeRayTrace(simulatePos, placementTarget.getPos()) : null;
            builder.add(placementTarget.getRequiredMaterials(context, target));
        }
        return builder.build();
    }
}
