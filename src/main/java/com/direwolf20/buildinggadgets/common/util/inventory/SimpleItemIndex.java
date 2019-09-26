package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.direwolf20.buildinggadgets.common.util.inventory.SimpleItemIndex.MatchResult;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset.Entry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Set;

public final class SimpleItemIndex implements IItemIndex<MatchResult> {
    private final Multimap<Item, IStackHandle> handleMap;
    private final Set<IInsertExtractProvider> insertExtractProviders;

    SimpleItemIndex(Multimap<Item, IStackHandle> handleMap, Set<IInsertExtractProvider> insertExtractProviders) {
        this.handleMap = handleMap;
        this.insertExtractProviders = insertExtractProviders;
    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        MatchResult requiredMatch = match(list, list.getRequiredItems(), true);
        if (! requiredMatch.isSuccess())
            return requiredMatch;
        boolean anyOption = false;
        for (ImmutableMultiset<UniqueItem> option : list.getItemOptions()) {
            anyOption = true;
            MatchResult optionMatch = match(list, option, true);
            if (optionMatch.isSuccess())
                return new SuccessResult(list, option);
        }
        return anyOption ? new FailureResult(list) : requiredMatch;
    }

    private MatchResult match(MaterialList list, ImmutableMultiset<UniqueItem> multiset, boolean simulate) {
        for (Entry<UniqueItem> entry : multiset.entrySet()) {
            int remainingCount = entry.getCount();
            for (IStackHandle handle : handleMap.get(entry.getElement().getItem())) {
                if (remainingCount <= 0)
                    break;
                int match = handle.match(entry.getElement(), remainingCount, simulate);
                if (match > 0)
                    remainingCount -= match;
            }
            if (remainingCount > 0) {
                ItemStack stack = entry.getElement().createStack(remainingCount);
                for (IInsertExtractProvider provider : insertExtractProviders) {
                    stack = provider.extract(stack, simulate);
                    remainingCount = stack.getCount();
                    if (stack.isEmpty())
                        break;
                }
            }
            if (remainingCount > 0)
                return new FailureResult(list);
        }
        return new SuccessResult(list, ImmutableMultiset.of());
    }

    @Override
    public boolean applyMatch(MatchResult result) {
        if (! result.isSuccess())
            return false;
        return match(result.getMatchedList(), result.getMatchedList().getRequiredItems(), false).isSuccess() &&
                match(result.getMatchedList(), result.getChosenOption(), false).isSuccess();
    }

    public static abstract class MatchResult implements IMatchResult {
        private final MaterialList matchedList;

        private MatchResult(MaterialList matchedList) {
            this.matchedList = matchedList;
        }

        @Override
        public MaterialList getMatchedList() {
            return matchedList;
        }

        public abstract ImmutableMultiset<UniqueItem> getChosenOption();
    }

    private static class SuccessResult extends MatchResult {
        private final ImmutableMultiset<UniqueItem> chosenOption;

        private SuccessResult(MaterialList matchedList, ImmutableMultiset<UniqueItem> chosenOption) {
            super(matchedList);
            this.chosenOption = chosenOption;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public ImmutableMultiset<UniqueItem> getChosenOption() {
            return chosenOption;
        }
    }

    private static class FailureResult extends MatchResult {
        private FailureResult(MaterialList matchedList) {
            super(matchedList);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public ImmutableMultiset<UniqueItem> getChosenOption() {
            return ImmutableMultiset.of();
        }
    }
}
