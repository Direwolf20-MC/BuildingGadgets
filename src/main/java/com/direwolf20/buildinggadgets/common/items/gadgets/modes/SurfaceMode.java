package com.direwolf20.buildinggadgets.common.items.gadgets.modes;

import com.direwolf20.buildinggadgets.common.building.Region;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SurfaceMode extends AbstractMode {
    public SurfaceMode(boolean isExchanging) { super(isExchanging); }

    @Override
    List<BlockPos> collect(PlayerEntity player, BlockPos playerPos, Direction side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        int bound = range / 2;

        // Grow the area. getXOffset will invert some math for us
        Region area = new Region(start).expand(
                bound * (1 - Math.abs(side.getXOffset())),
                bound * (1 - Math.abs(side.getYOffset())),
                bound * (1 - Math.abs(side.getZOffset()))
        );

        area.spliterator().forEachRemaining(coordinates::add);
        return coordinates;
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
