package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.collect.*;
import net.minecraft.nbt.CompoundNBT;

import java.util.Iterator;

class OrMaterialListEntry extends SubMaterialListEntry {
    static final MaterialListEntry.Serializer<SubMaterialListEntry> SERIALIZER = new SubMaterialListEntry.Serializer() {
        @Override
        protected SubMaterialListEntry create(ImmutableList<MaterialListEntry<?>> subEntries, CompoundNBT nbt, boolean persisted) {
            return new OrMaterialListEntry(subEntries);
        }
    }.setRegistryName(NBTKeys.OR_SERIALIZER_ID);

    OrMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries) {
        super(subEntries);
    }

    @Override
    public PeekingIterator<ImmutableMultiset<UniqueItem>> iterator() {
        if (getSubEntries().isEmpty())
            return Iterators.peekingIterator(Iterators.singletonIterator(ImmutableMultiset.of()));
        Iterator<MaterialListEntry<?>> entryIterator = getSubEntries().iterator();
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
}
