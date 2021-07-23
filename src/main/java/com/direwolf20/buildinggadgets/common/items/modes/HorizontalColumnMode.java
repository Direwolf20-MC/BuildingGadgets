package com.direwolf20.buildinggadgets.common.items.modes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class HorizontalColumnMode extends AbstractMode {
    public HorizontalColumnMode(boolean isExchanging) {
        super(isExchanging);
    }

    @Override
    List<BlockPos> collect(UseContext context, Player player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        Direction side = XYZ.isAxisY(context.getHitSide()) ? player.getDirection() : context.getHitSide().getOpposite();
        if( !isExchanging() ) {
            for (int i = 0; i < context.getRange(); i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
        } else {
            side = side.getClockWise();
            int halfRange = context.getRange() / 2;
            for (int i = -halfRange; i <= halfRange; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
        }

        return coordinates;
    }
}

