package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.common.construction.Mode;
import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import com.direwolf20.buildinggadgets.common.construction.XYZ;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HorizontalColumnMode extends Mode {
    public HorizontalColumnMode(boolean isExchanging) {
        super("horizontal_column", isExchanging);
    }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        Direction side = XYZ.isAxisY(context.getHitSide()) ? player.getHorizontalFacing() : context.getHitSide().getOpposite();
        if( !isExchanging() ) {
            for (int i = 0; i < context.getRange(); i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
        } else {
            side = side.rotateY();
            int halfRange = context.getRange() / 2;
            for (int i = -halfRange; i <= halfRange; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
        }

        return coordinates;
    }
}

