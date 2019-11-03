package com.direwolf20.buildinggadgets.common.building.tilesupport;

import com.direwolf20.buildinggadgets.common.registry.Registries.TileEntityData;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * Function creating {@link IAdditionalBlockData} from a given {@link TileEntity}.
 * <p>
 * The implementations registered to {@link TileEntityData#getTileDataFactories()} (via {@link net.minecraftforge.fml.InterModComms.IMCMessage}'s) will be sorted
 * according to the specified topological boundaries. When queried, they will be called successively until the first implementation returns a
 * non-null value. Therefore overriding an existing {@code IAdditionalBlockDataFactory} is as easy as requiring it to be run after your own implementation.
 * <p>
 * {@link TileEntity TileEntities} wishing for custom {@link IAdditionalBlockData} implementations should instead of registering an additional
 * {@code IAdditionalBlockDataFactory} implement {@link IAdditionalBlockDataProvider}. This is only intended for providing {@link IAdditionalBlockData} implementations
 * for {@link TileEntity TileEntities} who are beyond your own control.
 */
@FunctionalInterface
public interface IAdditionalBlockDataFactory {
    /**
     * Creates a new {@link IAdditionalBlockData} for the given tileEntity if supported by this {@code IAdditionalBlockDataFactory}.
     * @param tileEntity The {@link TileEntity} to provide {@link IAdditionalBlockData} for.
     * @return A new {@link IAdditionalBlockData} if this {@code IAdditionalBlockDataFactory} can create one for the given tileEntity or null if not.
     */
    @Nullable
    IAdditionalBlockData createDataFor(BlockState state, @Nullable TileEntity tileEntity);
}
