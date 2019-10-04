package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.google.common.collect.ImmutableMultiset;

public final class MatchResult {
    private final MaterialList matchedList;
    private final ImmutableMultiset<UniqueItem> foundItems;
    private final ImmutableMultiset<UniqueItem> chosenOption;
    private final boolean isSuccess;

    public static MatchResult success(MaterialList matchedList, ImmutableMultiset<UniqueItem> foundItems, ImmutableMultiset<UniqueItem> chosenOption) {
        return new MatchResult(matchedList, foundItems, chosenOption, true);
    }

    public static MatchResult failure() {
        return new MatchResult(MaterialList.empty(), ImmutableMultiset.of(), ImmutableMultiset.of(), false);
    }

    public static MatchResult failure(MaterialList matchedList, ImmutableMultiset<UniqueItem> foundItems, ImmutableMultiset<UniqueItem> chosenOption) {
        return new MatchResult(matchedList, foundItems, chosenOption, false);
    }

    MatchResult(MaterialList matchedList, ImmutableMultiset<UniqueItem> foundItems, ImmutableMultiset<UniqueItem> chosenOption, boolean isSuccess) {
        this.matchedList = matchedList;
        this.foundItems = foundItems;
        this.chosenOption = chosenOption;
        this.isSuccess = isSuccess;
    }

    public MaterialList getMatchedList() {
        return matchedList;
    }

    public ImmutableMultiset<UniqueItem> getFoundItems() {
        return foundItems;
    }

    public ImmutableMultiset<UniqueItem> getChosenOption() {
        return chosenOption;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
