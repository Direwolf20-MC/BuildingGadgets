package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.collect.*;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class OrMaterialListEntry extends SubMaterialListEntry {
    static final MaterialListEntry.Serializer<SubMaterialListEntry> SERIALIZER = new SubMaterialListEntry.Serializer() {
        @Override
        protected SubMaterialListEntry create(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> constantEntries, CompoundNBT nbt, boolean persisted) {
            return new OrMaterialListEntry(subEntries, constantEntries);
        }
    }.setRegistryName(NBTKeys.OR_SERIALIZER_ID);

    OrMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> constantEntries) {
        super(subEntries, constantEntries);
    }

    OrMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries) {
        super(subEntries);
    }

    @Override
    public PeekingIterator<ImmutableMultiset<UniqueItem>> iterator() {
        if (! getAllSubEntries().findFirst().isPresent())
            return Iterators.peekingIterator(Iterators.singletonIterator(ImmutableMultiset.of()));
        Iterator<MaterialListEntry<?>> entryIterator = getAllSubEntries().iterator();
        return Iterators.peekingIterator(new AbstractIterator<ImmutableMultiset<UniqueItem>>() {
            private Iterator<ImmutableMultiset<UniqueItem>> itemIterator;

            @Override
            protected ImmutableMultiset<UniqueItem> computeNext() {
                if (itemIterator == null) {
                    if (entryIterator.hasNext())
                        itemIterator = entryIterator.next().iterator();
                    else
                        return endOfData();
                }
                if (! itemIterator.hasNext()) {
                    itemIterator = null;
                    return computeNext();
                }
                return itemIterator.next();
            }
        });
    }

    @Override
    public MaterialListEntry.Serializer<SubMaterialListEntry> getSerializer() {
        return SERIALIZER;
    }

    @Override
    protected List<MaterialListEntry<?>> orderAndSimplifyEntries(List<OrMaterialListEntry> orEntries, List<AndMaterialListEntry> andEntries, List<SimpleMaterialListEntry> simpleEntries) {
        List<MaterialListEntry<?>> res = super.orderAndSimplifyEntries(orEntries, andEntries, simpleEntries);
        for (OrMaterialListEntry orEntry:orEntries) {
            List<OrMaterialListEntry> innerOrEntries = new ArrayList<>(orEntry.getSubEntries().size());
            List<AndMaterialListEntry> innerAndEntries = new ArrayList<>(orEntry.getSubEntries().size());
            List<SimpleMaterialListEntry> innerSimpleEntries = new ArrayList<>(orEntry.getConstantEntries().size());
            List<MaterialListEntry<?>> innerRemainder = orEntry.orderAndSimplifyEntries(innerOrEntries, innerAndEntries, innerSimpleEntries);
            orEntries.addAll(innerOrEntries);
            andEntries.addAll(innerAndEntries);
            simpleEntries.addAll(innerSimpleEntries);
            res.addAll(innerRemainder);
        };
        return res;
    }

    @Override
    protected SubMaterialListEntry createFrom(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> constantEntry) {
        return new OrMaterialListEntry(subEntries, constantEntry);
    }
}
