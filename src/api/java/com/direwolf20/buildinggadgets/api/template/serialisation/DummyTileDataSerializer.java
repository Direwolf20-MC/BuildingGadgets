package com.direwolf20.buildinggadgets.api.template.serialisation;

import com.direwolf20.buildinggadgets.api.template.building.tilesupport.DummyTileEntityData;
import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class DummyTileDataSerializer extends ForgeRegistryEntry<ITileDataSerializer> implements ITileDataSerializer {
    DummyTileDataSerializer() {
        super();
    }

    @Override
    public NBTTagCompound serialize(ITileEntityData data, boolean persisted) {
        return new NBTTagCompound();
    }

    @Override
    public ITileEntityData deserialize(NBTTagCompound tagCompound, boolean persisted) {
        return DummyTileEntityData.INSTANCE;
    }
}
