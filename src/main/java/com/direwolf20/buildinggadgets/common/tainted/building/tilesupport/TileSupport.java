package com.direwolf20.buildinggadgets.common.tainted.building.tilesupport;

import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries.TileEntityData;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
import com.google.common.base.MoreObjects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.Iterator;
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

    public static ITileEntityData createTileData(@Nullable BlockEntity te) {
        if (te == null)
            return dummyTileEntityData();
        ITileEntityData res;

        for (Iterator<ITileDataFactory> it = TileEntityData.getTileDataFactories().iterator(); it.hasNext(); ) {
            ITileDataFactory factory = it.next();
            res = factory.createDataFor(te);
            if (res != null)
                return res;
        }

        return dummyTileEntityData();
    }

    public static ITileEntityData createTileData(BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        return createTileData(te);
    }

    public static BlockData createBlockData(BlockState state, @Nullable BlockEntity te) {
        return new BlockData(Objects.requireNonNull(state), createTileData(te));
    }

    public static BlockData createBlockData(BlockGetter world, BlockPos pos) {
        return new BlockData(world.getBlockState(pos), createTileData(world, pos));
    }

    private static class DataProviderFactory implements ITileDataFactory {
        @Nullable
        @Override
        public ITileEntityData createDataFor(BlockEntity tileEntity) {
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
        public boolean placeIn(BuildContext context, BlockState state, BlockPos position) {
            return context.getWorld().setBlock(position, state, 0);
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
