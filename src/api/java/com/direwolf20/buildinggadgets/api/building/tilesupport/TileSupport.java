package com.direwolf20.buildinggadgets.api.building.tilesupport;

import com.direwolf20.buildinggadgets.api.Registries.TileEntityData;
import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.SerialisationSupport;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;

public final class TileSupport {
    private TileSupport() {}

    private static ITileDataFactory DATA_PROVIDER_FACTORY = new DataProviderFactory();

    /**
     * Returns an adapter for {@link ITileDataFactory} and {@link ITileDataProvider}. If a {@link TileEntity} is an instance of {@link ITileDataProvider} this {@link ITileDataFactory}
     * will return the data created by the given {@link ITileDataProvider}.
     * <p>
     * Notice that this will by default be registered to be the last {@link ITileDataFactory} called in order to allow mods to override the data returned by a {@link TileEntity} itself.
     *
     * @return An {@link ITileDataFactory} which will return {@link ITileEntityData} instances provided by {@link TileEntity TileEntities} implementing {@link ITileDataProvider}
     */
    public static ITileDataFactory dataProviderFactory() {
        return DATA_PROVIDER_FACTORY;
    }

    public static ITileEntityData createTileData(IWorld world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null)
            return dummyTileEntityData();
        ITileEntityData res;
        for (ITileDataFactory factory : TileEntityData.getTileDataFactories()) {
            res = factory.createDataFor(te);
            if (res != null)
                return res;
        }
        return dummyTileEntityData();
    }

    public static BlockData createBlockData(IWorld world, BlockPos pos) {
        return new BlockData(world.getBlockState(pos), createTileData(world, pos));
    }

    private static class DataProviderFactory implements ITileDataFactory {
        @Nullable
        @Override
        public ITileEntityData createDataFor(TileEntity tileEntity) {
            if (tileEntity instanceof ITileDataProvider)
                return ((ITileDataProvider) tileEntity).createTileData();
            return null;
        }
    }

    private static final ITileEntityData DUMMY_TILE_ENTITY_DATA = new ITileEntityData() {
        @Override
        public ITileDataSerializer getSerializer() {
            return SerialisationSupport.dummyDataSerializer();
        }

        @Override
        public boolean placeIn(IBuildContext context, BlockState state, BlockPos position) {
            return context.getWorld().setBlockState(position, state, 0);
        }
    };

    public static ITileEntityData dummyTileEntityData() {
        return DUMMY_TILE_ENTITY_DATA;
    }
}
