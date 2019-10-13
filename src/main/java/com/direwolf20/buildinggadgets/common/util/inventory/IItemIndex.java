package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.inventory.IUniqueObject;
import com.google.common.collect.Multiset;

public interface IItemIndex {
    default Multiset<IUniqueObject<?>> insert(Multiset<IUniqueObject<?>> items) {
        return insert(items, false);
    }

    //returns the remaining items
    Multiset<IUniqueObject<?>> insert(Multiset<IUniqueObject<?>> items, boolean simulate);

    void reIndex();

    MatchResult tryMatch(MaterialList list);

    default MatchResult tryMatch(Multiset<IUniqueObject<?>> items) {
        return tryMatch(MaterialList.of(items));
    }

    boolean applyMatch(MatchResult result);

    default boolean applyMatch(MaterialList list) {
        MatchResult result = tryMatch(list);
        return result.isSuccess() && applyMatch(result);
    }
}
