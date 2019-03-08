package com.direwolf20.buildinggadgets.api.template.tilesupport;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ITileDataSerializer extends IForgeRegistryEntry<ITileDataSerializer> {
    public NBTTagCompound toNBT(ITileEntityData data);

    public ITileEntityData fromNBT(NBTTagCompound nbt);

    public Class<?> getDataClass();
}
