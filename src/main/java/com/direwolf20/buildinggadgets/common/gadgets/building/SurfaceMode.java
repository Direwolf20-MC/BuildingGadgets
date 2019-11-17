package com.direwolf20.buildinggadgets.common.gadgets.building;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SurfaceMode extends AbstractMode {
    public SurfaceMode() { super(false); }

    @Override
    List<BlockPos> collect(EntityPlayer player, BlockPos playerPos, EnumFacing side, int range, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();
        int bound = range / 2;

        // Grow the area. getFrontOffset will invert some math for us
        AxisAlignedBB area = new AxisAlignedBB(start).grow(
                bound * (1 - Math.abs(side.getFrontOffsetX())),
                bound * (1 - Math.abs(side.getFrontOffsetY())),
                bound * (1 - Math.abs(side.getFrontOffsetZ()))
        );

        Iterable<BlockPos> blockList = BlockPos.getAllInBox((int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1);
        for (BlockPos pos : blockList)
            coordinates.add(pos);

        return coordinates;
    }

    /**
     * @implNote I've intentionally not feature matched this as the original
     *           implementation made no sense. This should now make more sense.
     */
    @Override
    public boolean validator(World world, BlockPos pos, BlockPos lookingAt, IBlockState setBlock, boolean isFuzzy) {
        // Do our default checks, then do our more complex fuzzy aware checks.
        boolean firstValidation = super.validator(world, pos, lookingAt, setBlock, isFuzzy);

        IBlockState startState = world.getBlockState(lookingAt);
        return (!isFuzzy ? world.isAirBlock(pos) : world.getBlockState(pos) != startState) && firstValidation;
    }
}
