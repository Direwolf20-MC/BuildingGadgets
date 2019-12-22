package com.direwolf20.buildinggadgets.common.gadgets.history;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class HistoryEntry {
    private BlockPos pos;
    private IBlockState state;
    private IBlockState pasteState;

    public HistoryEntry(BlockPos pos, IBlockState state, @Nullable IBlockState pasteState) {
        this.pos = pos;
        this.state = state;
        this.pasteState = pasteState;
    }

    public HistoryEntry(BlockPos pos, IBlockState state) {
        this(pos, state, null);
    }

    public IBlockState getState() {
        return state;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public IBlockState getPasteState() {
        return pasteState;
    }
}
