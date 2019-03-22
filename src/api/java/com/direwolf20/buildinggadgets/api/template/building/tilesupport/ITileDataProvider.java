package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

/**
 * Represents an {@link java.util.function.Supplier Supplier&lt;ITileEntityData&gt;} which can be used for creating {@link ITileEntityData} instances.
 * <p>
 * A {@link net.minecraft.tileentity.TileEntity} implementing this interface will be queried from {@link DataProviderFactory} in order to create {@link ITileEntityData}.
 * It may still choose to return null.
 */
@FunctionalInterface
public interface ITileDataProvider {
    /**
     * @return A new {@link ITileEntityData} or null if non should be created.
     */
    ITileEntityData createTileData();
}
