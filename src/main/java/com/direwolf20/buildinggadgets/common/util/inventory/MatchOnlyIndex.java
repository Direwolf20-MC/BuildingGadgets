package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.google.common.collect.Multiset;

public final class MatchOnlyIndex implements IItemIndex {
    private final IItemIndex delegate;

    public MatchOnlyIndex(IItemIndex delegate) {
        this.delegate = delegate;
    }

    @Override
    public void reIndex() {
        delegate.reIndex();
    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        return delegate.tryMatch(list);
    }

    @Override
    public void insert(Multiset<UniqueItem> items) {

    }

    @Override
    public boolean applyMatch(MatchResult result) {
        return result.isSuccess();
    }
}
