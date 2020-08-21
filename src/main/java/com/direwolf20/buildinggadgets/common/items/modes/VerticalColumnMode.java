package com.direwolf20.buildinggadgets.common.items.modes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class VerticalColumnMode extends AbstractMode {
    public VerticalColumnMode(boolean isExchanging) {
        super(isExchanging);
    }

    @Override
    List<BlockPos> collect(UseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // If up or down, full height from start block
        int halfRange = context.getRange() / 2;

        if( XYZ.isAxisY(context.getHitSide()) ) {
            // The exchanger handles the Y completely differently :sad: means more code
            if( isExchanging() ) {
                Direction playerFacing = player.getHorizontalFacing();
                for (int i = -halfRange; i <= halfRange; i++)
                    coordinates.add(XYZ.extendPosSingle(i, start, playerFacing, XYZ.fromFacing(playerFacing)));
            } else {
                for (int i = 0; i < context.getRange(); i++)
                    coordinates.add(XYZ.extendPosSingle(i, start, context.getHitSide(), XYZ.Y));
            }
        // Else, half and half
        } else {
            for (int i = -halfRange; i <= halfRange; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, context.getHitSide(), XYZ.Y));
        }

        return coordinates;
    }

    /**
     * We need to modify where the offset is for this mode as when looking at any
     * face that isn't up or down, we need to push the offset back into the block
     * and ignore placeOnTop as this mode does the action by default.
     */
    @Override
    public BlockPos withOffset(BlockPos pos, Direction side, boolean placeOnTop) {
        return XYZ.isAxisY(side) ? super.withOffset(pos, side, placeOnTop) : pos;
    }
}

