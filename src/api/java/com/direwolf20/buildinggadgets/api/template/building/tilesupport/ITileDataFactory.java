package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

import com.direwolf20.buildinggadgets.api.Registries;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * Function creating {@link ITileEntityData} from a given {@link TileEntity}.
 * <p>
 * The implementations registered to {@link Registries#getTileDataFactories()} (in
 * {@link com.direwolf20.buildinggadgets.api.registry.OrderedRegistryEvent OrderedRegistryEvent<ITileDataFactory>}) will be sorted
 * according to the specified topological boundaries. When queried, they will be called successively until the first implementation returns a
 * non-null value. Therefore overriding an existing {@code ITileDataFactory} is as easy as requiring it to be run after your own implementation.
 * <p>
 * {@link TileEntity TileEntities} wishing for custom {@link ITileEntityData} implementations should instead of registering an additional
 * {@code ITileDataFactory} implement {@link ITileDataProvider}. This is only intended for providing {@link ITileEntityData} implementations
 * for {@link TileEntity TileEntities} who are beyond your own control.
 */
@FunctionalInterface
public interface ITileDataFactory {
    /**
     * Creates a new {@link ITileEntityData} for the given tileEntity if supported by this {@code ITileDataFactory}.
     * @param tileEntity The {@link TileEntity} to provide {@link ITileEntityData} for.
     * @return A new {@link ITileEntityData} if this {@code ITileDataFactory} can create one for the given tileEntity or null if not.
     */
    @Nullable
    ITileEntityData createDataFor(TileEntity tileEntity);
}
