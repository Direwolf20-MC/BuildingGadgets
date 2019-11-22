package com.direwolf20.buildinggadgets.common.gadgets.building;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.List;

public class HorizontalColumnMode extends AbstractMode {
    public HorizontalColumnMode(boolean isExchanging) {
        super(isExchanging);
    }

    @Override
    List<BlockPos> collect(EntityPlayer player, BlockPos playerPos, EnumFacing side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        side = XYZ.isAxisY(side) ? player.getHorizontalFacing() : side.getOpposite();
        if( !isExchanging() ) {
            for (int i = 0; i < range; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
        } else {
            side = side.rotateY();
            int halfRange = range / 2;
            for (int i = -halfRange; i <= halfRange; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
        }

        return coordinates;
    }
}

