package com.direwolf20.buildinggadgets.common.gadgets.building;

import com.direwolf20.buildinggadgets.common.config.Config;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class BuildToMeMode extends AbstractMode {
    public BuildToMeMode() { super(false); }

    @Override
    public List<BlockPos> collect(EntityPlayer player, BlockPos playerPos, EnumFacing side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        XYZ facingXYZ = XYZ.fromFacing(side);

        int startCoord = XYZ.posToXYZ(start, facingXYZ);
        int playerCoord = XYZ.posToXYZ(playerPos, facingXYZ);

        // Clamp the value to the max range of the gadgets raytrace
        int difference = Math.max(0, Math.min((int) Config.rayTraceRange, Math.abs(startCoord - playerCoord)));
        for( int i = 0; i < difference; i ++)
            coordinates.add(XYZ.extendPosSingle(i, start, side, facingXYZ));

        return coordinates;
    }
}
