package com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IUniqueObjectSerializer extends IForgeRegistryEntry<IUniqueObjectSerializer> {
    CompoundTag serialize(IUniqueObject<?> item, boolean persisted);

    IUniqueObject<?> deserialize(CompoundTag res);

    JsonSerializer<IUniqueObject<?>> asJsonSerializer(boolean printName, boolean extended);

    JsonDeserializer<IUniqueObject<?>> asJsonDeserializer();
}
