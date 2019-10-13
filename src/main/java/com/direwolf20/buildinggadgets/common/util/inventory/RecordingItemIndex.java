package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.inventory.IUniqueObject;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public final class RecordingItemIndex implements IItemIndex {
    private final IItemIndex other;
    private Multiset<IUniqueObject<?>> extractedItems;
    private Multiset<IUniqueObject<?>> insertedItems;

    public RecordingItemIndex(IItemIndex other) {
        this.other = other;
        this.extractedItems = HashMultiset.create();
        this.insertedItems = HashMultiset.create();
    }

    @Override
    public Multiset<IUniqueObject<?>> insert(Multiset<IUniqueObject<?>> items, boolean simulate) {
        Multiset<IUniqueObject<?>> res = other.insert(items, simulate);
        if (! simulate)
            insertedItems.addAll(items);
        return res;
    }

    @Override
    public void reIndex() {
        other.reIndex();
        insertedItems.clear();
        extractedItems.clear();
    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        return other.tryMatch(MaterialList.and(list, MaterialList.of(extractedItems)));
    }

    @Override
    public MatchResult tryMatch(Multiset<IUniqueObject<?>> items) {
        return other.tryMatch(ImmutableMultiset.<IUniqueObject<?>>builder()
                .addAll(items)
                .addAll(extractedItems)
                .build());
    }

    @Override
    public boolean applyMatch(MatchResult result) {
        if (result.isSuccess()) {
            extractedItems.addAll(Multisets.difference(result.getChosenOption(), extractedItems));
            return true;
        }
        return false;
    }

    public Multiset<IUniqueObject<?>> getExtractedItems() {
        return Multisets.unmodifiableMultiset(extractedItems);
    }

    public Multiset<IUniqueObject<?>> getInsertedItems() {
        return Multisets.unmodifiableMultiset(insertedItems);
    }
}
