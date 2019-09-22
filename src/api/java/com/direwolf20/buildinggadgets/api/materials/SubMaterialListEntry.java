package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.JsonKeys;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Objects;

abstract class SubMaterialListEntry implements MaterialListEntry<SubMaterialListEntry> {
    private final ImmutableList<MaterialListEntry<?>> subEntries;

    protected SubMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries) {
        this.subEntries = Objects.requireNonNull(subEntries, "Cannot construct a SubMaterialListEntry without a list of Sub-MaterialEntries!");
    }

    protected ImmutableList<MaterialListEntry<?>> getSubEntries() {
        return subEntries;
    }

    protected static abstract class Serializer extends ForgeRegistryEntry<MaterialListEntry.Serializer<SubMaterialListEntry>> implements MaterialListEntry.Serializer<SubMaterialListEntry> {
        @Override
        public SubMaterialListEntry readFromNBT(CompoundNBT nbt, boolean persisted) {
            ListNBT list = nbt.getList(NBTKeys.KEY_SUB_ENTRIES, NBT.TAG_COMPOUND);
            ImmutableList.Builder<MaterialListEntry<?>> entryBuilder = ImmutableList.builder();
            for (INBT subEntry : list) {
                MaterialListEntry<?> entry = MaterialList.readEntry((CompoundNBT) subEntry, persisted);
                entryBuilder.add(entry);
            }
            return create(entryBuilder.build(), nbt, persisted);
        }

        @Override
        public CompoundNBT writeToNBT(SubMaterialListEntry entry, boolean persisted) {
            ListNBT list = new ListNBT();
            for (MaterialListEntry<?> subEntry : entry.getSubEntries()) {
                list.add(MaterialList.writeEntry(subEntry, persisted));
            }
            CompoundNBT nbt = new CompoundNBT();
            nbt.put(NBTKeys.KEY_SUB_ENTRIES, list);
            return nbt;
        }

        @Override
        public JsonSerializer<SubMaterialListEntry> asJsonSerializer(boolean printName, boolean extended) {
            return (src, typeOfSrc, context) -> {
                JsonArray ar = new JsonArray();
                for (MaterialListEntry entry : src.getSubEntries()) {
                    @SuppressWarnings("unchecked") //I ignore generics on purpose here, as this will always be the correct type - it's it's own serializer
                            JsonElement element = entry.getSerializer().asJsonSerializer(printName, extended).serialize(entry, entry.getClass(), context);
                    JsonObject obj;
                    if (element.isJsonObject() && ! element.getAsJsonObject().has(JsonKeys.MATERIAL_ENTRY_TYPE)) {
                        obj = element.getAsJsonObject();
                    } else if (element.isJsonArray()) {
                        obj = new JsonObject();
                        obj.add(JsonKeys.MATERIAL_ENTRIES, element.getAsJsonArray());
                    } else {
                        obj = new JsonObject();
                        obj.add(JsonKeys.MATERIAL_ENTRY, element);
                    }
                    obj.add(JsonKeys.MATERIAL_ENTRY_TYPE, context.serialize(entry.getSerializer().getRegistryName()));
                }
                return ar;
            };
        }

        protected abstract SubMaterialListEntry create(ImmutableList<MaterialListEntry<?>> subEntries, CompoundNBT nbt, boolean persisted);
    }
}
