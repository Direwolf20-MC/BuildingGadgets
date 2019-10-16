package com.direwolf20.buildinggadgets.common.inventory.materials;

import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.util.ref.JsonKeys;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

abstract class SubMaterialListEntry implements MaterialListEntry<SubMaterialListEntry> {
    private final ImmutableList<MaterialListEntry<?>> subEntries;
    private final ImmutableList<SimpleMaterialListEntry> constantEntries;
    private boolean simplified;

    public SubMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> simpleEntries, boolean simplified) {
        this.subEntries = Objects.requireNonNull(subEntries, "Cannot construct a SubMaterialListEntry without a list of Sub-MaterialEntries!");
        this.constantEntries = Objects.requireNonNull(simpleEntries, "Cannot construct a SubMaterialListEntry without a list of constant Entries!");
        this.simplified = simplified;
    }

    protected SubMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> simpleEntries) {
        this(subEntries, simpleEntries, false);
    }

    public SubMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries) {
        this(subEntries, ImmutableList.of());
    }

    protected Stream<MaterialListEntry<?>> getAllSubEntries() {
        return Streams.concat(subEntries.stream(), constantEntries.stream());
    }

    @Override
    public MaterialListEntry<?> simplify() {
        if (simplified)
            return this;
        List<OrMaterialListEntry> orEntries = new ArrayList<>(subEntries.size());
        List<AndMaterialListEntry> andEntries = new ArrayList<>(subEntries.size());
        List<SimpleMaterialListEntry> simpleEntries = new ArrayList<>(constantEntries);
        List<MaterialListEntry<?>> remainder = orderAndSimplifyEntries(orEntries, andEntries, simpleEntries);
        SimpleMaterialListEntry constantEntry = combine(simpleEntries);
        if (orEntries.isEmpty() && andEntries.isEmpty() && remainder.isEmpty())
            return constantEntry;
        return createFrom(ImmutableList.<MaterialListEntry<?>>builder()
                .addAll(andEntries)
                .addAll(orEntries)
                .addAll(remainder)
                .build(),
                constantEntry.getItems().isEmpty() ? ImmutableList.of() : ImmutableList.of(constantEntry), true);
    }

    protected abstract List<MaterialListEntry<?>> orderAndSimplifyEntries(
            List<OrMaterialListEntry> orEntries,
            List<AndMaterialListEntry> andEntries,
            List<SimpleMaterialListEntry> simpleEntries);

    protected ImmutableList<MaterialListEntry<?>> getSubEntries() {
        return subEntries;
    }

    protected ImmutableList<SimpleMaterialListEntry> getConstantEntries() {
        return constantEntries;
    }

    protected SimpleMaterialListEntry getCombinedConstantEntry() {
        return combine(constantEntries);
    }

    private SimpleMaterialListEntry combine(List<SimpleMaterialListEntry> simpleEntries) {
        if (simpleEntries.size() == 1)
            return simpleEntries.get(0);
        ImmutableMultiset.Builder<IUniqueObject<?>> builder = ImmutableMultiset.builder();
        for (SimpleMaterialListEntry entry:simpleEntries) {
            builder.addAll(entry.getItems());
        };
        return new SimpleMaterialListEntry(builder.build());
    }

    protected abstract SubMaterialListEntry createFrom(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> constantEntry, boolean simplified);

    protected Iterable<ImmutableMultiset<IUniqueObject<?>>> viewOnlySubEntries() {
        return createFrom(getSubEntries(), ImmutableList.of(), simplified);
    }

    protected void pullUpInnerEntries(SubMaterialListEntry entry,
                                      List<OrMaterialListEntry> orEntries,
                                      List<AndMaterialListEntry> andEntries,
                                      List<SimpleMaterialListEntry> simpleEntries,
                                      List<MaterialListEntry<?>> remainder) {
        for (MaterialListEntry<?> subEntry : entry.getSubEntries()) {
            if (subEntry instanceof OrMaterialListEntry)
                orEntries.add((OrMaterialListEntry) subEntry);
            else if (subEntry instanceof AndMaterialListEntry)
                andEntries.add((AndMaterialListEntry) subEntry);
            else if (subEntry instanceof SimpleMaterialListEntry)
                simpleEntries.add((SimpleMaterialListEntry) subEntry);
            else
                remainder.add(subEntry);
        } ;
    }

    protected static abstract class Serializer extends ForgeRegistryEntry<MaterialListEntry.Serializer<SubMaterialListEntry>> implements MaterialListEntry.Serializer<SubMaterialListEntry> {
        @Override
        public SubMaterialListEntry readFromNBT(CompoundNBT nbt, boolean persisted) {
            ListNBT list = nbt.getList(NBTKeys.KEY_SUB_ENTRIES, NBT.TAG_COMPOUND);
            ImmutableList.Builder<MaterialListEntry<?>> entryBuilder = ImmutableList.builder();
            ImmutableList.Builder<SimpleMaterialListEntry> simpleBuilder = ImmutableList.builder();
            for (INBT subEntry : list) {
                MaterialListEntry<?> entry = MaterialList.readEntry((CompoundNBT) subEntry, persisted);
                if (entry instanceof SimpleMaterialListEntry)
                    simpleBuilder.add((SimpleMaterialListEntry) entry);
                else
                    entryBuilder.add(entry);
            }
            return create(entryBuilder.build(), simpleBuilder.build(), nbt, persisted);
        }

        @Override
        public CompoundNBT writeToNBT(SubMaterialListEntry entry, boolean persisted) {
            ListNBT list = new ListNBT();
            entry.getAllSubEntries()
                    .map(subEntry -> MaterialList.writeEntry(subEntry, persisted))
                    .forEach(list::add);
            CompoundNBT nbt = new CompoundNBT();
            nbt.put(NBTKeys.KEY_SUB_ENTRIES, list);
            return nbt;
        }

        @Override
        public JsonSerializer<SubMaterialListEntry> asJsonSerializer(boolean printName, boolean extended) {
            return (src, typeOfSrc, context) -> {
                JsonArray ar = new JsonArray();
                src.getAllSubEntries().forEach((MaterialListEntry entry) -> {
                    @SuppressWarnings("unchecked") //I ignore generics on purpose here, as this will always be the correct type - it's it's own serializer
                    JsonElement element = entry.getSerializer().asJsonSerializer(printName, extended).serialize(entry, entry.getClass(), context);
                    JsonObject obj = new JsonObject();
                    obj.add(JsonKeys.MATERIAL_ENTRY_TYPE, context.serialize(entry.getSerializer().getRegistryName()));
                    if (element.isJsonArray()) {
                        obj.add(JsonKeys.MATERIAL_ENTRIES, element.getAsJsonArray());
                    } else {
                        obj.add(JsonKeys.MATERIAL_ENTRY, element);
                    }
                    ar.add(obj);
                });
                return ar;
            };
        }

        protected abstract SubMaterialListEntry create(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> constantEntries, CompoundNBT nbt, boolean persisted);
    }
}
