package com.direwolf20.buildinggadgets.common.util.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockMap {

    public final BlockPos pos;
    public final BlockState state;
    public int xOffset = 0;
    public int yOffset = 0;
    public int zOffset = 0;

    public BlockMap(BlockPos blockPos, BlockState iBlockState) {
        pos = blockPos;
        state = iBlockState;
    }

    public BlockMap(BlockPos blockPos, BlockState iBlockState, int x, int y, int z) {
        pos = blockPos;
        state = iBlockState;
        xOffset = x;
        yOffset = y;
        zOffset = z;
    }

    public boolean equals(BlockMap map) {
        return (map.pos.equals(pos) && map.state.equals(state));
    }
}
