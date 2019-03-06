package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.template.tilesupport.ITileEntityData;
import net.minecraft.block.state.IBlockState;

public final class BlockData {
    private final IBlockState state;
    private final ITileEntityData tileData;

    public BlockData(IBlockState state, ITileEntityData tileData) {
        this.state = state;
        this.tileData = tileData;
    }

    public IBlockState getState() {
        return state;
    }

    public ITileEntityData getTileData() {
        return tileData;
    }
}
