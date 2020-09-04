package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

/**
 * An {@link IItemIndex} which instead of inserting or extracting Items from the backing {@link IItemIndex} keeps record of
 * everything that was attempted to be inserted and then simulates extraction/insertion of the combination of the "record" and
 * the new Items. Works just like {@link com.direwolf20.buildinggadgets.common.util.tools.SimulateEnergyStorage} - only for Items.
 *
 * @see com.direwolf20.buildinggadgets.common.util.tools.SimulateEnergyStorage
 */
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
}
