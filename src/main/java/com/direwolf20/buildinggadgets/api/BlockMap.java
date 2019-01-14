package com.direwolf20.buildinggadgets.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public final class BlockMap {

    private final BlockPos pos;
    private final IBlockState state;
    private final int xOffset;
    private final int yOffset;
    private final int zOffset;

    public BlockMap(BlockPos blockPos, IBlockState iBlockState) {
        this(blockPos, iBlockState, 0, 0, 0);
    }

    public BlockMap(BlockPos blockPos, IBlockState iBlockState, int x, int y, int z) {
        pos = blockPos;
        state = iBlockState;
        xOffset = x;
        yOffset = y;
        zOffset = z;
    }

    public boolean equals(BlockMap map) {
        return (map.pos.equals(pos) && map.state.equals(state));
    }

    public BlockPos getPos() {
        return pos;
    }

    public IBlockState getState() {
        return state;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getZOffset() {
        return zOffset;
    }
}
