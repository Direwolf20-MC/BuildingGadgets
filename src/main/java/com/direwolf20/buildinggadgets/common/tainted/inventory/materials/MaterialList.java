package com.direwolf20.buildinggadgets.common.tainted.inventory.materials;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.util.ref.JsonKeys;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.JsonBiDiSerializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.PeekingIterator;
import com.google.gson.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the required Item options for a single block, or a set of blocks. The option with the highest priority is listed
 * first, followed by the remaining options in descending priority order.
 */
public final class MaterialList implements Iterable<ImmutableMultiset<IUniqueObject<?>>> {

    private static final MaterialList EMPTY = new MaterialList();

    public static MaterialList deserialize(CompoundNBT nbt, boolean persisted) {
        return new MaterialList(readEntry(nbt, persisted));
    }

    public static MaterialList empty() {
        return EMPTY;
    }

    public static MaterialList of() {
        return empty();
    }

    public static MaterialList of(IUniqueObject<?>... items) {
        return simpleBuilder().add(items).build();
    }

    public static MaterialList of(Iterable<IUniqueObject<?>> items) {
        return simpleBuilder().addAll(items).build();
    }

    public static MaterialList and(MaterialList... materialLists) {
        return andBuilder().add(materialLists).build();
    }

    public static MaterialList or(MaterialList... materialLists) {
        return orBuilder().add(materialLists).build();
    }

    public static SimpleBuilder simpleBuilder() {
        return new SimpleBuilder();
    }

    public static SubEntryBuilder andBuilder() {
        return new SubEntryBuilder(AndMaterialListEntry::new);
    }

    public static SubEntryBuilder orBuilder() {
        return new SubEntryBuilder(OrMaterialListEntry::new);
    }

    @Nullable
    static MaterialListEntry.Serializer<?> getSerializerForId(ResourceLocation id) {
        MaterialListEntry.Serializer<?> serializer = null;
        if (id.equals(NBTKeys.SIMPLE_SERIALIZER_ID))
            serializer = SimpleMaterialListEntry.SERIALIZER;
        else if (id.equals(NBTKeys.OR_SERIALIZER_ID))
            serializer = OrMaterialListEntry.SERIALIZER;
        else if (id.equals(NBTKeys.AND_SERIALIZER_ID))
            serializer = AndMaterialListEntry.SERIALIZER;
        return serializer;
    }

    static MaterialListEntry<?> readEntry(CompoundNBT nbt, boolean persisted) {
        ResourceLocation id = new ResourceLocation(nbt.getString(NBTKeys.KEY_SERIALIZER));
        MaterialListEntry.Serializer<?> serializer = getSerializerForId(id);
        Preconditions.checkArgument(serializer != null,
                "Failed to recognize Serializer " + id +
                        "! If you believe you need another implementation, please contact us and we can sort something out!");
        return serializer.readFromNBT(nbt, persisted);
    }

    @SuppressWarnings("unchecked")
    //This is ok - the only unchecked is the implicit cast for writing the data which only uses the appropriate serializer
    static CompoundNBT writeEntry(MaterialListEntry entry, boolean persisted) {
        assert entry.getSerializer().getRegistryName() != null;
        CompoundNBT res = new CompoundNBT();
        res.putString(NBTKeys.KEY_SERIALIZER, entry.getSerializer().getRegistryName().toString());
        res.put(NBTKeys.KEY_DATA, entry.getSerializer().writeToNBT(entry, persisted));
        return res;
    }

    private final MaterialListEntry rootEntry;

    private MaterialList() {
        this(new SimpleMaterialListEntry(ImmutableMultiset.of()));
    }

    private MaterialList(MaterialListEntry rootEntry) {
        this.rootEntry = Objects.requireNonNull(rootEntry, "Cannot have a MaterialList without a root entry!");
    }

    private MaterialListEntry getRootEntry() {
        return rootEntry;
    }

    public Iterable<ImmutableMultiset<IUniqueObject<?>>> getItemOptions() {
        return rootEntry instanceof SubMaterialListEntry?
                ((SubMaterialListEntry) rootEntry).viewOnlySubEntries() :
                ImmutableList.of();
    }

    public ImmutableMultiset<IUniqueObject<?>> getRequiredItems() {
        SimpleMaterialListEntry simpleEntry = rootEntry instanceof SubMaterialListEntry?
                ((SubMaterialListEntry) rootEntry).getCombinedConstantEntry() :
                ((SimpleMaterialListEntry) rootEntry);
        return simpleEntry.getItems();
    }

    public CompoundNBT serialize(boolean persisted) {
        return writeEntry(rootEntry, persisted);
    }

    @Override
    @SuppressWarnings("unchecked") //The iterator is independent of the type anyway
    public PeekingIterator<ImmutableMultiset<IUniqueObject<?>>> iterator() {
        return getRootEntry().iterator();
    }

    public static final class SubEntryBuilder {
        private ImmutableList.Builder<MaterialListEntry<?>> subBuilder;
        private final Function<ImmutableList<MaterialListEntry<?>>, MaterialListEntry<?>> factory;

        private SubEntryBuilder(Function<ImmutableList<MaterialListEntry<?>>, MaterialListEntry<?>> factory) {
            this.subBuilder = ImmutableList.builder();
            this.factory = factory;
        }

        public SubEntryBuilder add(SimpleBuilder builder) {
            return add(builder.build());
        }

        public SubEntryBuilder add(SimpleBuilder... builders) {
            return addAllSimpleBuilders(Arrays.asList(builders));
        }

        public SubEntryBuilder add(SubEntryBuilder builder) {
            return add(builder.build());
        }

        public SubEntryBuilder add(SubEntryBuilder... builders) {
            return addAllSubBuilders(Arrays.asList(builders));
        }

        public SubEntryBuilder add(MaterialList element) {
            subBuilder.add(element.getRootEntry());
            return this;
        }

        public SubEntryBuilder add(MaterialList... elements) {
            return addAll(Arrays.asList(elements));
        }

        public SubEntryBuilder addItems(Multiset<IUniqueObject<?>> items) {
            subBuilder.add(new SimpleMaterialListEntry(ImmutableMultiset.copyOf(items)));
            return this;
        }

        public SubEntryBuilder addAllItems(Iterable<? extends Multiset<IUniqueObject<?>>> iterable) {
            iterable.forEach(this::addItems);
            return this;
        }

        public SubEntryBuilder addAll(Iterable<MaterialList> elements) {
            elements.forEach(this::add);
            return this;
        }

        public SubEntryBuilder addAll(Iterator<MaterialList> elements) {
            elements.forEachRemaining(this::add);
            return this;
        }

        public SubEntryBuilder addAllSimpleBuilders(Iterable<SimpleBuilder> iterable) {
            iterable.forEach(this::add);
            return this;
        }

        public SubEntryBuilder addAllSubBuilders(Iterable<SubEntryBuilder> iterable) {
            iterable.forEach(this::add);
            return this;
        }

        public MaterialList build() {
            return new MaterialList(factory.apply(subBuilder.build()).simplify());
        }
    }

    public static final class SimpleBuilder {
        private ImmutableMultiset.Builder<IUniqueObject<?>> requiredItems;

        private SimpleBuilder() {
            requiredItems = ImmutableMultiset.builder();
        }

        public SimpleBuilder addItem(IUniqueObject<?> item, int count) {
            requiredItems.addCopies(item, count);
            return this;
        }

        public SimpleBuilder addItem(IUniqueObject<?> item) {
            return addItem(item, 1);
        }

        public SimpleBuilder addAll(Iterable<IUniqueObject<?>> items) {
            requiredItems.addAll(items);
            return this;
        }

        public SimpleBuilder setCount(IUniqueObject<?> element, int count) {
            requiredItems.setCount(element, count);
            return this;
        }

        public SimpleBuilder add(IUniqueObject<?>... elements) {
            requiredItems.add(elements);
            return this;
        }

        public SimpleBuilder addAll(Iterator<? extends IUniqueObject<?>> elements) {
            requiredItems.addAll(elements);
            return this;
        }

        public MaterialList build() {
            return new MaterialList(new SimpleMaterialListEntry(requiredItems.build()));
        }
    }

    public static final class JsonSerializer implements JsonBiDiSerializer<MaterialList> {
        private final boolean printName;
        private final boolean extended;

        public JsonSerializer(boolean printName, boolean extended) {
            this.printName = printName;
            this.extended = extended;
        }

        @Override
        @SuppressWarnings("unchecked") // only called on the entry itself... this is ok
        public JsonElement serialize(MaterialList src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject res = new JsonObject();
            res.add(JsonKeys.MATERIAL_LIST_ROOT_TYPE, context.serialize(src.getRootEntry().getSerializer().getRegistryName()));
            res.add(JsonKeys.MATERIAL_LIST_ROOT_ENTRY, src.getRootEntry().getSerializer().asJsonSerializer(printName, extended).serialize(src.getRootEntry(), src.getRootEntry().getClass(), context));
            return res;
        }

        @Override
        public MaterialList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonNull())
                return MaterialList.empty();
            JsonObject object = json.getAsJsonObject();
            MaterialListEntry.Serializer<?> serializer = getSerializerForId(context.deserialize(object.get(JsonKeys.MATERIAL_LIST_ROOT_TYPE), ResourceLocation.class));
            if (serializer == null)
                return MaterialList.empty();
            JsonDeserializer<?> jsonSerializer = serializer.asJsonDeserializer();
            return new MaterialList((MaterialListEntry) jsonSerializer.deserialize(object.get(JsonKeys.MATERIAL_LIST_ROOT_ENTRY), MaterialListEntry.class, context));
        }
    }
}
