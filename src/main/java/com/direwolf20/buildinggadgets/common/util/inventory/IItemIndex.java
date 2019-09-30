package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.google.common.collect.Multiset;

public interface IItemIndex {

    void insert(Multiset<UniqueItem> items);

    void reIndex();

    MatchResult tryMatch(MaterialList list);

    default MatchResult tryMatch(Multiset<UniqueItem> items) {
        return tryMatch(MaterialList.of(items));
    }

    boolean applyMatch(MatchResult result);

    default boolean applyMatch(MaterialList list) {
        MatchResult result = tryMatch(list);
        return result.isSuccess() && applyMatch(result);
    }
}
