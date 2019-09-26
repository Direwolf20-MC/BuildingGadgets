package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsAPI;
import com.direwolf20.buildinggadgets.api.util.JsonKeys;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.PeekingIterator;
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

class SimpleMaterialListEntry implements MaterialListEntry<SimpleMaterialListEntry> {
    static final MaterialListEntry.Serializer<SimpleMaterialListEntry> SERIALIZER = new Serializer().setRegistryName(NBTKeys.SIMPLE_SERIALIZER_ID);
    private final ImmutableMultiset<UniqueItem> items;

    SimpleMaterialListEntry(ImmutableMultiset<UniqueItem> items) {
        this.items = Objects.requireNonNull(items, "Cannot have a SimpleMaterialListEntry without any Materials!");
    }

    ImmutableMultiset<UniqueItem> getItems() {
        return items;
    }

    @Override
    public PeekingIterator<ImmutableMultiset<UniqueItem>> iterator() {
        return Iterators.peekingIterator(Iterators.singletonIterator(items));
    }

    @Override
    public MaterialListEntry.Serializer<SimpleMaterialListEntry> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public SimpleMaterialListEntry simplify() {
        return this;
    }

    private static class Serializer extends ForgeRegistryEntry<MaterialListEntry.Serializer<SimpleMaterialListEntry>> implements MaterialListEntry.Serializer<SimpleMaterialListEntry> {
        @Override
        public SimpleMaterialListEntry readFromNBT(CompoundNBT nbt, boolean persisted) {
            ListNBT nbtList = nbt.getList(NBTKeys.KEY_DATA, NBT.TAG_COMPOUND);
            ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();
            for (INBT nbtEntry : nbtList) {
                CompoundNBT compoundEntry = (CompoundNBT) nbtEntry;
                Serializer serializer;
                if (! compoundEntry.getString(NBTKeys.KEY_SERIALIZER).equals(NBTKeys.SIMPLE_UNIQUE_ITEM_ID)) {
                    BuildingGadgetsAPI.LOG.error("Found unknown UniqueItem serializer {}. This version can only handle {}! Skipping!",
                            compoundEntry.getString(NBTKeys.KEY_SERIALIZER), NBTKeys.SIMPLE_UNIQUE_ITEM_ID);
                    continue;
                }
                builder.addCopies(
                        UniqueItem.SERIALIZER.deserialize((compoundEntry.getCompound(NBTKeys.KEY_DATA))),
                        compoundEntry.getInt(NBTKeys.KEY_COUNT));
            }
            return new SimpleMaterialListEntry(builder.build());
        }

        @Override
        public CompoundNBT writeToNBT(SimpleMaterialListEntry listEntry, boolean persisted) {
            CompoundNBT res = new CompoundNBT();
            ListNBT nbtList = new ListNBT();
            for (Entry<UniqueItem> entry : listEntry.getItems().entrySet()) {
                CompoundNBT nbtEntry = new CompoundNBT();
                nbtEntry.putString(NBTKeys.KEY_SERIALIZER, entry.getElement().getSerializer().getRegistryName().toString());
                nbtEntry.put(NBTKeys.KEY_DATA, entry.getElement().getSerializer().serialize(entry.getElement(), persisted));
                nbtEntry.putInt(NBTKeys.KEY_COUNT, entry.getCount());
                nbtList.add(nbtEntry);
            }
            res.put(NBTKeys.KEY_DATA, nbtList);
            return res;
        }

        @Override
        public JsonSerializer<SimpleMaterialListEntry> asJsonSerializer(boolean printName, boolean extended) {
            return (src, typeOfSrc, context) -> {
                Multiset<UniqueItem> set = src.getItems();
                JsonArray jsonArray = new JsonArray();
                for (Entry<UniqueItem> entry : set.entrySet()) {
                    JsonElement element = entry
                            .getElement()
                            .getSerializer()
                            .asJsonSerializer(entry.getCount(), printName, extended)
                            .serialize(entry.getElement(), entry.getElement().getClass(), context);
                    JsonObject obj;
                    if (element.isJsonObject()) {
                        obj = element.getAsJsonObject();
                    } else {
                        obj = new JsonObject();
                        obj.add(JsonKeys.MATERIAL_ENTRY, element);
                    }
                    obj.addProperty(JsonKeys.MATERIAL_ENTRY_TYPE, entry.getElement().getSerializer().getRegistryName().toString());
                    jsonArray.add(obj);
                }
                JsonObject res = new JsonObject();
                res.add(JsonKeys.MATERIAL_ENTRIES, jsonArray);
                return res;
            };
        }
    }
}
