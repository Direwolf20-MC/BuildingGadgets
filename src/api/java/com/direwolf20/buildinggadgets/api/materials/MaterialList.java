package com.direwolf20.buildinggadgets.api.materials;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import java.util.Collection;

public final class MaterialList { //Todo fully implement MaterialList system
    private static final MaterialList EMPTY = new MaterialList();

    public static MaterialList empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    private final ImmutableMultiset<UniqueItem> requiredItems;

    private MaterialList() {
        this(ImmutableMultiset.of());
    }

    private MaterialList(Multiset<UniqueItem> requiredItems) {
        this.requiredItems = ImmutableMultiset.copyOf(requiredItems);
    }

    public ImmutableMultiset<UniqueItem> getRequiredItems() {
        return requiredItems;
    }

    public static final class Builder {
        private Multiset<UniqueItem> requiredItems;

        private Builder() {
            requiredItems = HashMultiset.create();
        }

        public Builder addItem(UniqueItem item, int count) {
            requiredItems.add(item, count);
            return this;
        }

        public Builder addItem(UniqueItem item) {
            return addItem(item, 1);
        }

        public Builder addAll(Collection<UniqueItem> items) {
            requiredItems.addAll(items);
            return this;
        }

        public MaterialList build() {
            return new MaterialList(requiredItems);
        }
    }
}
