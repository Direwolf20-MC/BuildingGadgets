package com.direwolf20.buildinggadgets.api.template.serialisation;

import com.direwolf20.buildinggadgets.api.template.building.tilesupport.DummyTileEntityData;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class SerialisationSupport {
    private SerialisationSupport() {}

    private static final ITileDataSerializer DUMMY_TILE_DATA_SERIALIZER = new DummyTileDataSerializer();

    public static ITileDataSerializer dummyDataSerializer() {
        return DUMMY_TILE_DATA_SERIALIZER;
    }

    private static final class DummyTileDataSerializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {

        private DummyTileDataSerializer() {
            super();
        }

        @Override
        public CompoundNBT serialize(ITileEntityData data, boolean persisted) {
            return new CompoundNBT();
        }

        @Override
        public ITileEntityData deserialize(CompoundNBT tagCompound, boolean persisted) {
            return DummyTileEntityData.INSTANCE;
        }
    }
}
