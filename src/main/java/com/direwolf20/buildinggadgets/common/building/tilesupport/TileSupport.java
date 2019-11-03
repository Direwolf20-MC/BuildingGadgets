package com.direwolf20.buildinggadgets.common.building.tilesupport;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.registry.Registries.TileEntityData;
import com.direwolf20.buildinggadgets.common.template.SerialisationSupport;
import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;

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

    public static ITileEntityData createTileData(BlockState state, @Nullable TileEntity te) {
        if (te == null)
            return dummyTileEntityData();
        ITileEntityData res;
        for (ITileDataFactory factory : TileEntityData.getTileDataFactories()) {
            res = factory.createDataFor(state, te);
            if (res != null)
                return res;
        }
        return dummyTileEntityData();
    }

    public static ITileEntityData createTileData(IBlockReader world, BlockState state, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return createTileData(state, te);
    }

    public static ITileEntityData createTileData(IBlockReader world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return createTileData(world, state, pos);
    }

    public static BlockData createBlockData(BlockState state, @Nullable TileEntity te) {
        return new BlockData(Objects.requireNonNull(state), createTileData(state, te));
    }

    public static BlockData createBlockData(IBlockReader world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return new BlockData(world.getBlockState(pos), createTileData(world, state, pos));
    }

    private static class DataProviderFactory implements ITileDataFactory {
        @Nullable
        @Override
        public ITileEntityData createDataFor(BlockState state, @Nullable TileEntity tileEntity) {
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

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .toString();
        }
    };

    public static ITileEntityData dummyTileEntityData() {
        return DUMMY_TILE_ENTITY_DATA;
    }
}
