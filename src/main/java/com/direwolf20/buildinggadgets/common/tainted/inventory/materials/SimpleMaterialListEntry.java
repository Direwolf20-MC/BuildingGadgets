package com.direwolf20.buildinggadgets.common.tainted.inventory.materials;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.tainted.registry.Registries;
import com.direwolf20.buildinggadgets.common.util.ref.JsonKeys;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.RegistryUtils;
import com.google.common.collect.*;
import com.google.common.collect.Multiset.Entry;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

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
        public SimpleMaterialListEntry readFromNBT(CompoundTag nbt, boolean persisted) {
            ListTag nbtList = nbt.getList(NBTKeys.KEY_DATA, Tag.TAG_COMPOUND);
            ImmutableMultiset.Builder<IUniqueObject<?>> builder = ImmutableMultiset.builder();
            for (Tag nbtEntry : nbtList) {
                CompoundTag compoundEntry = (CompoundTag) nbtEntry;
                IUniqueObjectSerializer serializer = persisted ?
                        RegistryUtils.getFromString(Registries.UNIQUE_DATA_SERIALIZER_REGISTRY.get(), compoundEntry.getString(NBTKeys.KEY_SERIALIZER)) :
                        RegistryUtils.getById(Registries.UNIQUE_DATA_SERIALIZER_REGISTRY.get(), compoundEntry.getInt(NBTKeys.KEY_SERIALIZER));
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
        public CompoundTag writeToNBT(SimpleMaterialListEntry listEntry, boolean persisted) {
            CompoundTag res = new CompoundTag();
            ListTag nbtList = new ListTag();
            for (Entry<IUniqueObject<?>> entry : listEntry.getItems().entrySet()) {
                CompoundTag nbtEntry = new CompoundTag();
                if (persisted)
                    nbtEntry.putString(NBTKeys.KEY_SERIALIZER, Registries.UNIQUE_DATA_SERIALIZER_REGISTRY.get().getKey(entry.getElement().getSerializer()).toString());
                else
                    nbtEntry.putInt(NBTKeys.KEY_SERIALIZER, RegistryUtils.getId(Registries.UNIQUE_DATA_SERIALIZER_REGISTRY.get(), entry.getElement().getSerializer()));
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
                    obj.add(JsonKeys.MATERIAL_LIST_ITEM_TYPE, context.serialize(Registries.UNIQUE_DATA_SERIALIZER_REGISTRY.get().getKey(entry.getElement().getSerializer())));
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
                    IUniqueObjectSerializer serializer = Registries.UNIQUE_DATA_SERIALIZER_REGISTRY.get().getValue(id);
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
