package com.direwolf20.buildinggadgets.common.building.tilesupport;

/**
 * Represents an {@link java.util.function.Supplier Supplier&lt;IAdditionalBlockData&gt;} which can be used for creating {@link IAdditionalBlockData} instances.
 * <p>
 * A {@link net.minecraft.tileentity.TileEntity} implementing this interface will be queried from {@link TileSupport#dataProviderFactory()} in order to create {@link IAdditionalBlockData}.
 * It may still choose to return null.
 */
@FunctionalInterface
public interface IAdditionalBlockDataProvider {
    /**
     * @return A new {@link IAdditionalBlockData} or null if non should be created.
     */
    IAdditionalBlockData createTileData();
}
