package com.direwolf20.buildinggadgets.common.inventory.materials;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.registry.Registries;
import com.direwolf20.buildinggadgets.common.util.ref.JsonKeys;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.RegistryUtils;
import com.google.common.collect.*;
import com.google.common.collect.Multiset.Entry;
import com.google.gson.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Comparator;
import java.util.Objects;

class SimpleMaterialListEntry implements MaterialListEntry<SimpleMaterialListEntry> {
    static final MaterialListEntry.Serializer<SimpleMaterialListEntry> SERIALIZER = new Serializer();
    private final ImmutableMultiset<IUniqueObject<?>> items;

    SimpleMaterialListEntry(ImmutableMultiset<IUniqueObject<?>> items) {
        this.items = Objects.requireNonNull(items, "Cannot have a SimpleMaterialListEntry without any Materials!");
    }

    ImmutableMultiset<IUniqueObject<?>> getItems() {
        return items;
    }

    @Override
    public PeekingIterator<ImmutableMultiset<IUniqueObject<?>>> iterator() {
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

    private static class Serializer implements MaterialListEntry.Serializer<SimpleMaterialListEntry> {
        private static final Comparator<Entry<IUniqueObject<?>>> COMPARATOR = Comparator
                .<Entry<IUniqueObject<?>>, ResourceLocation>comparing(e -> e.getElement().getObjectRegistryName())
                .thenComparingInt(Entry::getCount);
        @Override
        public SimpleMaterialListEntry readFromNBT(CompoundNBT nbt, boolean persisted) {
            ListNBT nbtList = nbt.getList(NBTKeys.KEY_DATA, NBT.TAG_COMPOUND);
            ImmutableMultiset.Builder<IUniqueObject<?>> builder = ImmutableMultiset.builder();
            for (INBT nbtEntry : nbtList) {
                CompoundNBT compoundEntry = (CompoundNBT) nbtEntry;
                IUniqueObjectSerializer serializer = persisted ?
                        RegistryUtils.getFromString(Registries.getUniqueObjectSerializers(), compoundEntry.getString(NBTKeys.KEY_SERIALIZER)) :
                        RegistryUtils.getById(Registries.getUniqueObjectSerializers(), compoundEntry.getInt(NBTKeys.KEY_SERIALIZER));
                if (serializer == null) {
                    BuildingGadgets.LOG.error("Found unknown UniqueItem serializer {}. Skipping!", compoundEntry.getString(NBTKeys.KEY_SERIALIZER));
                    continue;
                }
                builder.addCopies(
                        serializer.deserialize((compoundEntry.getCompound(NBTKeys.KEY_DATA))),
                        compoundEntry.getInt(NBTKeys.KEY_COUNT));
            }
            return new SimpleMaterialListEntry(builder.build());
        }

        @Override
        public CompoundNBT writeToNBT(SimpleMaterialListEntry listEntry, boolean persisted) {
            CompoundNBT res = new CompoundNBT();
            ListNBT nbtList = new ListNBT();
            for (Entry<IUniqueObject<?>> entry : listEntry.getItems().entrySet()) {
                CompoundNBT nbtEntry = new CompoundNBT();
                if (persisted)
                    nbtEntry.putString(NBTKeys.KEY_SERIALIZER, entry.getElement().getSerializer().getRegistryName().toString());
                else
                    nbtEntry.putInt(NBTKeys.KEY_SERIALIZER, RegistryUtils.getId(Registries.getUniqueObjectSerializers(), entry.getElement().getSerializer()));
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
                Multiset<IUniqueObject<?>> set = src.getItems();
                JsonArray jsonArray = new JsonArray();
                for (Entry<IUniqueObject<?>> entry : ImmutableList.sortedCopyOf(COMPARATOR, set.entrySet())) {
                    JsonElement element = entry.getElement()
                            .getSerializer()
                            .asJsonSerializer(printName, extended)
                            .serialize(entry.getElement(), entry.getElement().getClass(), context);
                    JsonObject obj = new JsonObject();
                    obj.add(JsonKeys.MATERIAL_LIST_ITEM_TYPE, context.serialize(entry.getElement().getSerializer().getRegistryName()));
                    obj.addProperty(JsonKeys.MATERIAL_LIST_ITEM_COUNT, entry.getCount());
                    obj.add(JsonKeys.MATERIAL_LIST_ITEM, element);
                    jsonArray.add(obj);
                }
                return jsonArray;
            };
        }

        @Override
        public JsonDeserializer<SimpleMaterialListEntry> asJsonDeserializer() {
            return (json, typeOfT, context) -> {
                JsonArray array = json.getAsJsonArray();
                ImmutableMultiset.Builder<IUniqueObject<?>> items = ImmutableMultiset.builder();
                for (JsonElement element : array) {
                    JsonObject object = element.getAsJsonObject();
                    ResourceLocation id = context.deserialize(object.get(JsonKeys.MATERIAL_LIST_ITEM_TYPE), ResourceLocation.class);
                    IUniqueObjectSerializer serializer = Registries.getUniqueObjectSerializers().getValue(id);
                    if (serializer == null)
                        continue;
                    int count = object.getAsJsonPrimitive(JsonKeys.MATERIAL_LIST_ITEM_COUNT).getAsInt();
                    IUniqueObject<?> item = serializer.asJsonDeserializer().deserialize(object.get(JsonKeys.MATERIAL_LIST_ITEM), IUniqueObject.class, context);
                    items.addCopies(item, count);
                }
                return new SimpleMaterialListEntry(items.build());
            };
        }

        @Override
        public ResourceLocation getRegistryName() {
            return NBTKeys.SIMPLE_SERIALIZER_ID;
        }
    }
}
