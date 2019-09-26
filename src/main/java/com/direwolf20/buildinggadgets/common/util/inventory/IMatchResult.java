package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;

public interface IMatchResult {
    boolean isSuccess();

    MaterialList getMatchedList();
}
