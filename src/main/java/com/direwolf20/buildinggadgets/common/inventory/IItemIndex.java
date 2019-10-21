package com.direwolf20.buildinggadgets.common.inventory;

import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.google.common.collect.Multiset;

/**
 * Represents Index for accessible Items. It allows for extraction/insertion into some kind of IUniqueObject container(s).
 * An Implementation must also handle the options represented by a MaterialList correctly - test all available options until the first one matches, or no other is left
 * to search.
 * <p>
 * To update this index with contents that were inserted/extracted independently, you'll need to call {@link #reIndex()}.
 *
 * @see PlayerItemIndex
 * @see CreativeItemIndex
 */
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
