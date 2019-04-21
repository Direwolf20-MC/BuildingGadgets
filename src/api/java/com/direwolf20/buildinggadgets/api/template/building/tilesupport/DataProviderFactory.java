package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * Adapter singleton for {@link ITileDataFactory} and {@link ITileDataProvider}. If a {@link TileEntity} is an instance of {@link ITileDataProvider} this {@link ITileDataFactory}
 * will return the data created by the given {@link ITileDataProvider}.
 * <p>
 * Notice that this will by default be registered to be the last {@link ITileDataFactory} called in order to allow mods to override the data returned by a {@link TileEntity} itself.
 */
public enum DataProviderFactory implements ITileDataFactory {
    INSTANCE;
    @Nullable
    @Override
    public ITileEntityData createDataFor(TileEntity tileEntity) {
        if (tileEntity instanceof ITileDataProvider)
            return ((ITileDataProvider) tileEntity).createTileData();
        return null;
    }
}
