package com.direwolf20.buildinggadgets.common.construction.modes;

import com.direwolf20.buildinggadgets.common.construction.ModeUseContext;
import com.direwolf20.buildinggadgets.common.schema.BoundingBox;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.stream.Collectors;

public class SurfaceMode extends Mode {
    public SurfaceMode(boolean isExchanging) { super("surface", isExchanging); }

    @Override
    List<BlockPos> collect(ModeUseContext context, PlayerEntity player, BlockPos start) {

        int bound = context.getRange() / 2;
        BoundingBox region = new BoundingBox(start).expand(
                bound * (1 - Math.abs(context.getHitSide().getXOffset())),
                bound * (1 - Math.abs(context.getHitSide().getYOffset())),
                bound * (1 - Math.abs(context.getHitSide().getZOffset()))
        );

        return region.stream().collect(Collectors.toList());
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
