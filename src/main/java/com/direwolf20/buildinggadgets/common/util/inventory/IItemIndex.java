package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;

public interface IItemIndex<T extends IMatchResult> {
    T tryMatch(MaterialList list);

    boolean applyMatch(T result);

    default boolean applyMatch(MaterialList list) {
        T result = tryMatch(list);
        return result.isSuccess() && applyMatch(result);
    }
}
