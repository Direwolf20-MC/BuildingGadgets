package com.direwolf20.buildinggadgets.api.materials.inventory;

import com.google.gson.JsonSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IUniqueObjectSerializer extends IForgeRegistryEntry<IUniqueObjectSerializer> {
    CompoundNBT serialize(IUniqueObject<?> item, boolean persisted);

    IUniqueObject<?> deserialize(CompoundNBT res);

    JsonSerializer<IUniqueObject<?>> asJsonSerializer(int count, boolean printName, boolean extended);
}
