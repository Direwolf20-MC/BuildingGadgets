package com.direwolf20.buildinggadgets.common.items.modes;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.ConnectedSurface;
import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SurfaceMode extends AbstractMode {
    public SurfaceMode(boolean isExchanging) { super(isExchanging); }

    @Override
    List<BlockPos> collect(UseContext context, PlayerEntity player, BlockPos start) {

        int bound = context.getRange() / 2;

        // Grow the area. getXOffset will invert some math for us
        Region area = new Region(start).expand(
                bound * (1 - Math.abs(context.getHitSide().getXOffset())),
                bound * (1 - Math.abs(context.getHitSide().getYOffset())),
                bound * (1 - Math.abs(context.getHitSide().getZOffset()))
        );

        IPositionPlacementSequence blockPos = ConnectedSurface.create(context.getWorld(), start, context.getHitSide().getOpposite(), context.getRange(), context.isFuzzy());
        List<BlockPos> coordinates = new ArrayList<>(blockPos.collect());

//        area.spliterator().forEachRemaining(coordinates::add);
        return coordinates;
    }

    @Override
    public boolean validator(PlayerEntity player, BlockPos pos, UseContext context) {
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
