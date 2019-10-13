package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.inventory.IUniqueObject;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import java.util.Iterator;

public final class CreativeItemIndex implements IItemIndex {
    @Override
    public Multiset<IUniqueObject<?>> insert(Multiset<IUniqueObject<?>> items, boolean simulate) {
        return items;
    }

    @Override
    public void reIndex() {

    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        Iterator<ImmutableMultiset<IUniqueObject<?>>> it = list.iterator();
        ImmutableMultiset<IUniqueObject<?>> chosen = it.hasNext() ? it.next() : ImmutableMultiset.of();
        return MatchResult.success(list, chosen, chosen);
    }

    @Override
    public boolean applyMatch(MatchResult result) {
        return result.isSuccess();
    }
}
