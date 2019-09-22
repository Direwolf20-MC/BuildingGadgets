package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.JsonKeys;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.PeekingIterator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.NoSuchElementException;
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
        return new PeekingIterator<ImmutableMultiset<UniqueItem>>() {
            private boolean advanced = false;

            @Override
            public ImmutableMultiset<UniqueItem> peek() {
                if (advanced)
                    throw new NoSuchElementException();
                return items;
            }

            @Override
            public ImmutableMultiset<UniqueItem> next() {
                ImmutableMultiset<UniqueItem> res = peek();
                advanced = true;
                return res;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return ! advanced;
            }
        };
    }

    @Override
    public MaterialListEntry.Serializer<SimpleMaterialListEntry> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends ForgeRegistryEntry<MaterialListEntry.Serializer<SimpleMaterialListEntry>> implements MaterialListEntry.Serializer<SimpleMaterialListEntry> {
        @Override
        public SimpleMaterialListEntry readFromNBT(CompoundNBT nbt, boolean persisted) {
            ListNBT nbtList = nbt.getList(NBTKeys.KEY_DATA, NBT.TAG_COMPOUND);
            ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();
            for (INBT nbtEntry : nbtList) {
                CompoundNBT compoundEntry = (CompoundNBT) nbtEntry;
                builder.addCopies(
                        UniqueItem.deserialize((compoundEntry.getCompound(NBTKeys.KEY_DATA))),
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
                nbtEntry.put(NBTKeys.KEY_DATA, entry.getElement().serialize(true));
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
                    JsonObject obj = new JsonObject();
                    UniqueItem element = entry.getElement();
                    Item item = element.getItem();
                    if (printName)
                        obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_NAME, I18n.format(item.getTranslationKey(new ItemStack(item, entry.getCount()))));
                    if (extended || ! printName)
                        obj.add(JsonKeys.MATERIAL_LIST_ITEM_ID, context.serialize(element.getRegistryName()));
                    if (extended) {
                        CompoundNBT nbt = element.getTag();
                        if (nbt != null)
                            obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_NBT, element.getTag().toString());
                    }
                    obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_COUNT, entry.getCount());
                    jsonArray.add(obj);
                }
                JsonObject res = new JsonObject();
                res.add(JsonKeys.MATERIAL_ENTRIES, jsonArray);
                return res;
            };
        }
    }
}
