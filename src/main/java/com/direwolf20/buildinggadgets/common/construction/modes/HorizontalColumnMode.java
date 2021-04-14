package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
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

        Direction side = context.getHitSide().getAxis() == Direction.Axis.Y
            ? player.getDirection()
            : context.getHitSide().getOpposite();
        if (!this.isExchanging()) {
            for (int i = 0; i < context.getRange(); i++) {
                coordinates.add(start.relative(side, i));
            }
        } else {
            side = side.getClockWise();
            int halfRange = context.getRange() / 2;
            for (int i = -halfRange; i <= halfRange; i++) {
                coordinates.add(start.relative(side, i));
            }
        }

        return coordinates;
    }
}

