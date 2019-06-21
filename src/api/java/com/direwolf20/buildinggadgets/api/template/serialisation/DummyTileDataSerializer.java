package com.direwolf20.buildinggadgets.api.template.serialisation;

import com.direwolf20.buildinggadgets.api.template.building.tilesupport.DummyTileEntityData;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class DummyTileDataSerializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("buildinggadgets:dummy_tile_data_serializer");
    DummyTileDataSerializer() {
        super();
        setRegistryName(REGISTRY_NAME);
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
