package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.collect.*;
import net.minecraft.nbt.CompoundNBT;

import java.util.*;
import java.util.stream.Collectors;

class AndMaterialListEntry extends SubMaterialListEntry {
    static final MaterialListEntry.Serializer<SubMaterialListEntry> SERIALIZER = new SubMaterialListEntry.Serializer() {
        @Override
        protected SubMaterialListEntry create(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> constantEntries, CompoundNBT nbt, boolean persisted) {
            return new AndMaterialListEntry(subEntries, constantEntries);
        }
    }.setRegistryName(NBTKeys.AND_SERIALIZER_ID);


    AndMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> simpleEntries) {
        super(subEntries, simpleEntries);
    }

    AndMaterialListEntry(ImmutableList<MaterialListEntry<?>> subEntries) {
        super(subEntries);
    }

    @Override
    public PeekingIterator<ImmutableMultiset<UniqueItem>> iterator() {
        if (! getAllSubEntries().findFirst().isPresent())
            return Iterators.peekingIterator(Iterators.singletonIterator(ImmutableMultiset.of()));
        LinkedList<MaterialEntryWrapper> list = getAllSubEntries()
                .map(MaterialEntryWrapper::new)
                .collect(Collectors.toCollection(LinkedList::new));
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

    @Override
    protected List<MaterialListEntry<?>> orderAndSimplifyEntries(List<OrMaterialListEntry> orEntries, List<AndMaterialListEntry> andEntries, List<SimpleMaterialListEntry> simpleEntries) {
        List<MaterialListEntry<?>> res = super.orderAndSimplifyEntries(orEntries, andEntries, simpleEntries);
        for (AndMaterialListEntry andEntry: andEntries) {
            List<OrMaterialListEntry> innerOrEntries = new ArrayList<>(andEntry.getSubEntries().size());
            List<AndMaterialListEntry> innerAndEntries = new ArrayList<>(andEntry.getSubEntries().size());
            List<SimpleMaterialListEntry> innerSimpleEntries = new ArrayList<>(andEntry.getConstantEntries().size());
            List<MaterialListEntry<?>> innerRemainder = andEntry.orderAndSimplifyEntries(innerOrEntries, innerAndEntries, innerSimpleEntries);
            orEntries.addAll(innerOrEntries);
            andEntries.addAll(innerAndEntries);
            simpleEntries.addAll(innerSimpleEntries);
            res.addAll(innerRemainder);
        };
        return res;
    }

    @Override
    protected SubMaterialListEntry createFrom(ImmutableList<MaterialListEntry<?>> subEntries, ImmutableList<SimpleMaterialListEntry> constantEntry) {
        return new AndMaterialListEntry(subEntries, constantEntry);
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
