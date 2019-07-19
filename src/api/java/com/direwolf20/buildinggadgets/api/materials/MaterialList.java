package com.direwolf20.buildinggadgets.api.materials;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

public final class MaterialList {
    private static final MaterialList EMPTY = new MaterialList();

    public static MaterialList empty() {
        return EMPTY;
    }

    private final Multiset<UniqueItem> requiredItems;

    private MaterialList() {
        this(ImmutableMultiset.of());
    }

    private MaterialList(Multiset<UniqueItem> requiredItems) {
        this.requiredItems = requiredItems;
    }
}
