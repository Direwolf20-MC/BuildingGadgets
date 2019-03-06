package com.direwolf20.buildinggadgets.api.template.tilesupport;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class DataProviderFactory implements ITileDataFactory {
    @Nullable
    @Override
    public ITileEntityData createDataFor(TileEntity tileEntity) {
        if (tileEntity instanceof ITileDataProvider)
            return ((ITileDataProvider) tileEntity).createTileData();
        return null;
    }
}
