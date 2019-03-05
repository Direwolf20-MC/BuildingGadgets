package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.template.tilesupport.ITileEntityData;
import net.minecraft.block.state.IBlockState;

public final class BlockData {
    private final IBlockState state;
    private final ITileEntityData data;

    public BlockData(IBlockState state, ITileEntityData data) {
        this.state = state;
        this.data = data;
    }

    public IBlockState getState() {
        return state;
    }

    public ITileEntityData getData() {
        return data;
    }
}
