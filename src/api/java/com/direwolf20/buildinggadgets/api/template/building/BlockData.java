package com.direwolf20.buildinggadgets.api.template.building;

import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
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
