package com.direwolf20.buildinggadgets.api.template.serialisation;

import com.direwolf20.buildinggadgets.api.template.building.tilesupport.DummyTileEntityData;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class DummyTileDataSerializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {
    DummyTileDataSerializer() {
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
