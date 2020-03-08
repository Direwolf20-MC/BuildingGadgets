package com.direwolf20.buildinggadgets.common.items.gadgets.modes;

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
    List<BlockPos> collect(PlayerEntity player, BlockPos playerPos, Direction side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        // If up or down, full height from start block
        int halfRange = range / 2;

        if( XYZ.isAxisY(side) ) {
            // The exchanger handles the Y completely differently :sad: means more code
            if( isExchanging() ) {
                side = player.getHorizontalFacing();
                for (int i = -halfRange; i <= halfRange; i++)
                    coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.fromFacing(side)));
            } else {
                for (int i = 0; i < range; i++)
                    coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.Y));
            }
        // Else, half and half
        } else {
            for (int i = -halfRange; i <= halfRange; i++)
                coordinates.add(XYZ.extendPosSingle(i, start, side, XYZ.Y));
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

