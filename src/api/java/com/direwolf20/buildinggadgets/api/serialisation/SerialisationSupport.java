package com.direwolf20.buildinggadgets.api.serialisation;

import com.direwolf20.buildinggadgets.api.building.tilesupport.ITileEntityData;
import com.direwolf20.buildinggadgets.api.building.tilesupport.NBTTileEntityData;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.google.common.base.Preconditions;
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
            return TileSupport.dummyTileEntityData();
        }
    }

    private static final ITileDataSerializer NBT_TILE_DATA_SERIALIZER = new NBTTileEntityDataSerializer();

    public static ITileDataSerializer nbtTileDataSerializer() {
        return NBT_TILE_DATA_SERIALIZER;
    }

    private static final class NBTTileEntityDataSerializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {
        private NBTTileEntityDataSerializer() {}

        @Override
        public CompoundNBT serialize(ITileEntityData data, boolean persisted) {
            Preconditions.checkArgument(data instanceof NBTTileEntityData);
            return ((NBTTileEntityData) data).getNBT();
        }

        @Override
        public ITileEntityData deserialize(CompoundNBT tagCompound, boolean persisted) {
            return new NBTTileEntityData(tagCompound);
        }
    }

}
