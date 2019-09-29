package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Iterator;
import java.util.List;

public final class SimpleItemIndex implements IItemIndex {
    private final Multimap<Item, IStackHandle> handleMap;
    private final List<IInsertExtractProvider> insertExtractProviders;

    SimpleItemIndex(Multimap<Item, IStackHandle> handleMap, List<IInsertExtractProvider> insertExtractProviders) {
        this.handleMap = handleMap;
        this.insertExtractProviders = insertExtractProviders;
    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        MatchResult result = null;
        for (ImmutableMultiset<UniqueItem> multiset : list) {
            result = match(list, multiset, true);
            if (result.isSuccess())
                return MatchResult.success(list, result.getFoundItems(), multiset);
        }
        return result == null ? MatchResult.success(list, ImmutableMultiset.of(), ImmutableMultiset.of()) : evaluateFailingOptionFoundItems(list);
    }

    private MatchResult evaluateFailingOptionFoundItems(MaterialList list) {
        Multiset<UniqueItem> multiset = HashMultiset.create();
        for (ImmutableMultiset<UniqueItem> option : list.getItemOptions()) {
            for (Entry<UniqueItem> entry : option.entrySet()) {
                multiset.setCount(entry.getElement(), Math.max(multiset.count(entry.getElement()), entry.getCount()));
            }
        }
        multiset.addAll(list.getRequiredItems());
        MatchResult result = match(list, multiset, true);
        if (result.isSuccess())
            throw new RuntimeException("This should not be possible! The the content changed between matches?!?");
        Iterator<ImmutableMultiset<UniqueItem>> it = list.iterator();
        return it.hasNext() ? MatchResult.failure(list, result.getFoundItems(), it.next()) : result;
    }

    private MatchResult match(MaterialList list, Multiset<UniqueItem> multiset, boolean simulate) {
        ImmutableMultiset.Builder<UniqueItem> availableBuilder = ImmutableMultiset.builder();
        boolean failure = false;
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
                if (! stack.isEmpty()) {
                    for (IInsertExtractProvider provider : insertExtractProviders) {
                        stack = provider.extract(stack, simulate);
                        remainingCount = stack.getCount();
                        if (stack.isEmpty())
                            break;
                    }
                }
            }
            remainingCount = Math.max(0, remainingCount);
            if (remainingCount > 0)
                failure = true;
            availableBuilder.addCopies(entry.getElement(), entry.getCount() - remainingCount);
        }
        if (failure)
            return MatchResult.failure(list, availableBuilder.build(), ImmutableMultiset.of());
        return MatchResult.success(list, availableBuilder.build(), ImmutableMultiset.of());
    }

    @Override
    public boolean applyMatch(MatchResult result) {
        if (! result.isSuccess())
            return false;
        return match(result.getMatchedList(), result.getChosenOption(), false).isSuccess();
    }

}
