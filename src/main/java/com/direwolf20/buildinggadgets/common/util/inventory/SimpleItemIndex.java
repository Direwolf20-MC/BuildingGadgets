package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.google.common.collect.*;
import com.google.common.collect.Multiset.Entry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class SimpleItemIndex implements IItemIndex {
    private Multimap<Item, IStackHandle> handleMap;
    private List<IInsertExtractProvider> insertExtractProviders;
    private final ItemStack stack;
    private final PlayerEntity player;

    public SimpleItemIndex(ItemStack stack, PlayerEntity player) {
        this.stack = stack;
        this.player = player;
        reIndex();
    }

    @Override
    public void insert(Multiset<UniqueItem> items) {
        Multiset<UniqueItem> copy = HashMultiset.create(items);
        for (IItemHandler handler : InventoryHelper.getHandlers(stack, player)) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                Multiset<UniqueItem> toRemove = HashMultiset.create();
                for (Multiset.Entry<UniqueItem> entry : copy.entrySet()) {
                    int remainingCount = entry.getCount();
                    if (stack.isEmpty()) {
                        ItemStack insertStack = entry.getElement().createStack(entry.getCount());
                        remainingCount = handler.insertItem(i, insertStack, false).getCount();
                        IStackHandle handle = new StackHandlerHandle(handler, i);
                        if (! handleMap.containsEntry(insertStack.getItem(), handle))
                            handleMap.put(insertStack.getItem(), handle);
                    } else if (entry.getElement().matches(stack)) {
                        ItemStack copyStack = stack.copy();
                        copyStack.setCount(entry.getCount());
                        remainingCount = handler.insertItem(i, copyStack, false).getCount();
                    }
                    if (remainingCount < entry.getCount()) {
                        toRemove.add(entry.getElement(), entry.getCount() - remainingCount);
                        stack = handler.getStackInSlot(i);
                    }
                }
                for (Multiset.Entry<UniqueItem> entry : toRemove.entrySet()) {
                    copy.remove(entry.getElement(), entry.getCount());
                }
                if (copy.isEmpty())
                    return;
            }
        }
    }

    @Override
    public void reIndex() {
        this.handleMap = InventoryHelper.indexMap(stack, player);
        this.insertExtractProviders = ImmutableList.of();
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
            Collection<IStackHandle> entries = handleMap.get(entry.getElement().getItem());
            List<IStackHandle> toRemove = new LinkedList<>();
            for (IStackHandle handle : entries) {
                if (remainingCount <= 0)
                    break;
                int match = handle.match(entry.getElement(), remainingCount, simulate);
                if (match > 0)
                    remainingCount -= match;
                if (handle.shouldCleanup())
                    toRemove.add(handle);
            }
            entries.removeAll(toRemove);//cleanup
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
