package com.direwolf20.buildinggadgets.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockMap {

    public final BlockPos pos;
    public final IBlockState state;
    public int xOffset = 0;
    public int yOffset = 0;
    public int zOffset = 0;

    public BlockMap(BlockPos blockPos, IBlockState iBlockState) {
        pos = blockPos;
        state = iBlockState;
    }

    public BlockMap(BlockPos blockPos, IBlockState iBlockState, int x, int y, int z) {
        pos = blockPos;
        state = iBlockState;
        xOffset = x;
        yOffset = y;
        zOffset = z;
    }
}
