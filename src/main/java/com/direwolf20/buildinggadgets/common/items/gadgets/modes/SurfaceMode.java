package com.direwolf20.buildinggadgets.common.items.gadgets.modes;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SurfaceMode extends AbstractMode {
    public SurfaceMode(boolean isExchanging) { super(isExchanging); }

    @Override
    List<BlockPos> collect(PlayerEntity player, BlockPos playerPos, Direction side, int range, BlockPos start) {
        int bound = range / 2;

        // Grow the area. getFrontOffset will invert some math for us
        AxisAlignedBB area = new AxisAlignedBB(start).grow(
                bound * (1 - Math.abs(side.getXOffset())),
                bound * (1 - Math.abs(side.getYOffset())),
                bound * (1 - Math.abs(side.getZOffset()))
        );

        Stream<BlockPos> blockList = BlockPos.getAllInBox((int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1);
        return blockList.collect(Collectors.toList());
    }

    /**
     * @implNote I've intentionally not feature matched this as the original
     *           implementation made no sense. This should now make more sense.
     */
    @Override
    public boolean validator(PlayerEntity player, BlockPos pos, UseContext context) {
        // Do our default checks, then do our more complex fuzzy aware checks.
        boolean firstValidation = super.validator(player, pos, context);

        BlockState startState = context.getWorldState(context.getStartPos());
        return (!context.isFuzzy() ? context.getWorld().isAirBlock(pos) : context.getWorldState(pos) != startState) && firstValidation;
    }
}
