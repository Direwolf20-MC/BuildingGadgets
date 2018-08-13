package com.direwolf20.buildinggadgets.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockMap {

    public final BlockPos pos;
    public final IBlockState state;

    public BlockMap(BlockPos blockPos, IBlockState iBlockState) {
        pos = blockPos;
        state = iBlockState;
    }
}
