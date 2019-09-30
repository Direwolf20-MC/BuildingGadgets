package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public final class RecordingItemIndex implements IItemIndex {
    private final IItemIndex other;
    private Multiset<UniqueItem> extractedItems;
    private Multiset<UniqueItem> insertedItems;

    public RecordingItemIndex(IItemIndex other) {
        this.other = other;
        this.extractedItems = HashMultiset.create();
        this.insertedItems = HashMultiset.create();
    }

    @Override
    public void insert(Multiset<com.direwolf20.buildinggadgets.api.materials.UniqueItem> items) {
        insertedItems.addAll(items);
        other.insert(items);
    }

    @Override
    public void reIndex() {
        other.reIndex();
        insertedItems.clear();
        extractedItems.clear();
    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        return other.tryMatch(list);
    }

    @Override
    public MatchResult tryMatch(Multiset<com.direwolf20.buildinggadgets.api.materials.UniqueItem> items) {
        return other.tryMatch(items);
    }

    @Override
    public boolean applyMatch(MatchResult result) {
        extractedItems.addAll(result.getChosenOption());
        return other.applyMatch(result);
    }

    public Multiset<UniqueItem> getExtractedItems() {
        return Multisets.unmodifiableMultiset(extractedItems);
    }

    public Multiset<UniqueItem> getInsertedItems() {
        return Multisets.unmodifiableMultiset(insertedItems);
    }
}
