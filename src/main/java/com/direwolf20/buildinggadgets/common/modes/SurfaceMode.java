package com.direwolf20.buildinggadgets.common.modes;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SurfaceMode extends Mode {
    public SurfaceMode(boolean isExchanging) { super(isExchanging); }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        int bound = context.getRange() / 2;

// @todo: put back ->
//        // Grow the area. getXOffset will invert some math for us
//        Region area = new Region(start).expand(
//                bound * (1 - Math.abs(context.getHitSide().getXOffset())),
//                bound * (1 - Math.abs(context.getHitSide().getYOffset())),
//                bound * (1 - Math.abs(context.getHitSide().getZOffset()))
//        );
//
//        area.spliterator().forEachRemaining(coordinates::add);
        return coordinates;
    }

    @Override
    public boolean validator(PlayerEntity player, BlockPos pos, ModeUseContext context) {
        // Do our default checks, then do our more complex fuzzy aware checks.
        boolean topRow = super.validator(player, pos, context);
        if( this.isExchanging() )
            return topRow;

        BlockState startState = context.getWorldState(context.getStartPos());
        if( context.isFuzzy() )
            return topRow && !context.getWorld().isAirBlock(pos.offset(context.getHitSide().getOpposite()));

        return topRow && context.getWorld().getBlockState(pos.offset(context.getHitSide().getOpposite())) == startState;
    }
}
