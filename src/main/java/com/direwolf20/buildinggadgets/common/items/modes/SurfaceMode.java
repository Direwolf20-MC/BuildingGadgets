package com.direwolf20.buildinggadgets.common.items.modes;

import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.direwolf20.buildinggadgets.common.tainted.building.placement.ConnectedSurface;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SurfaceMode extends AbstractMode {
    public SurfaceMode(boolean isExchanging) { super(isExchanging); }

    @Override
    List<BlockPos> collect(UseContext context, PlayerEntity player, BlockPos start) {
        int bound = context.getRange() / 2;

        // Grow the area. getXOffset will invert some math for us
        Region area = new Region(start).expand(
                bound * (1 - Math.abs(context.getHitSide().getStepX())),
                bound * (1 - Math.abs(context.getHitSide().getStepY())),
                bound * (1 - Math.abs(context.getHitSide().getStepZ()))
        );

        if (!context.isConnected()) {
            return area.stream().map(BlockPos::immutable).collect(Collectors.toList());
        }

        List<BlockPos> coords = new ArrayList<>();

        ConnectedSurface.create(area, context.getWorld(), pos -> isExchanging() ? pos : pos.relative(context.getHitSide().getOpposite()), start, context.getHitSide().getOpposite(), context.getRange(), context.isFuzzy())
                .spliterator()
                .forEachRemaining(coords::add);

        return coords;
    }

    @Override
    public boolean validator(PlayerEntity player, BlockPos pos, UseContext context) {
        // Do our default checks, then do our more complex fuzzy aware checks.
        boolean topRow = super.validator(player, pos, context);
        if( this.isExchanging() )
            return topRow;

        BlockState startState = context.getWorldState(context.getStartPos());
        if( context.isFuzzy() )
            return topRow && !context.getWorld().isEmptyBlock(pos.relative(context.getHitSide().getOpposite()));

        return topRow && context.getWorld().getBlockState(pos.relative(context.getHitSide().getOpposite())) == startState;
    }
}
