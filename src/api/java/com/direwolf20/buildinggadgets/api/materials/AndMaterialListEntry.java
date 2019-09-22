package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.collect.*;
import net.minecraft.nbt.CompoundNBT;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

class AndMaterialListEntry extends SubMaterialListEntry {
    static final MaterialListEntry.Serializer<SubMaterialListEntry> SERIALIZER = new SubMaterialListEntry.Serializer() {
        @Override
        protected SubMaterialListEntry create(ImmutableList<MaterialListEntry<?>> subEntries, CompoundNBT nbt, boolean persisted) {
            return new AndMaterialListEntry(subEntries);
        }
    }.setRegistryName(NBTKeys.AND_SERIALIZER_ID);

    AndMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries) {
        super(subEntries);
    }

    @Override
    public PeekingIterator<ImmutableMultiset<UniqueItem>> iterator() {
        if (getSubEntries().isEmpty())
            return Iterators.peekingIterator(Iterators.singletonIterator(ImmutableMultiset.of()));
        LinkedList<MaterialEntryWrapper> list = new LinkedList<>();
        for (MaterialListEntry<?> entry : getSubEntries()) {
            list.add(new MaterialEntryWrapper(entry));
        } ;
        return Iterators.peekingIterator(new AbstractIterator<ImmutableMultiset<UniqueItem>>() {
            private Deque<MaterialEntryWrapper> dequeue = list;

            @Override
            protected ImmutableMultiset<UniqueItem> computeNext() {
                if (dequeue.isEmpty())
                    return endOfData();
                ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();
                for (MaterialEntryWrapper wrapper : dequeue) {
                    if (wrapper.hasNext())
                        builder.addAll(wrapper.peek());
                }
                Iterator<MaterialEntryWrapper> revIt = dequeue.descendingIterator();
                MaterialEntryWrapper lastReset = null;
                while (revIt != null && revIt.hasNext()) {
                    MaterialEntryWrapper wrapper = revIt.next();
                    if (wrapper.hasNext()) {
                        wrapper.advance();
                        revIt = null;
                    } else {
                        wrapper.reset();
                        lastReset = wrapper;
                    }
                }
                if (lastReset == dequeue.getFirst()) {
                    dequeue.clear();
                }
                return builder.build();
            }
        });
    }

    @Override
    public MaterialListEntry.Serializer<SubMaterialListEntry> getSerializer() {
        return SERIALIZER;
    }

    private static final class MaterialEntryWrapper {
        private PeekingIterator<ImmutableMultiset<UniqueItem>> curIterator;
        private final MaterialListEntry<?> entry;

        private MaterialEntryWrapper(MaterialListEntry<?> entry) {
            this.entry = entry;
            this.curIterator = entry.iterator();
        }

        private ImmutableMultiset<UniqueItem> peek() {
            return curIterator.peek();
        }

        private boolean hasNext() {
            return curIterator.hasNext();
        }

        private void advance() {
            curIterator.next();
        }

        private void reset() {
            curIterator = entry.iterator();
        }
    }
}
