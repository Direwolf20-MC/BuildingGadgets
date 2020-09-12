package com.direwolf20.buildinggadgets.common.tainted.inventory;

import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IObjectHandle;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.google.common.collect.*;
import com.google.common.collect.Multiset.Entry;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

/**
 * Item Index representation all Items accessible for the Player by BuildingGadgets.
 * To allow for better performance, the Items in the player's Inventory are indexed by their Item and upon query only those with the appropriate Item need to be iterated.
 */
public final class PlayerItemIndex implements IItemIndex {
    //use a class map first, to allow for non-Item IUniqueObjects...
    private Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> handleMap;
    private List<IInsertProvider> insertProviders;
    private final ItemStack stack;
    private final PlayerEntity player;

    public PlayerItemIndex(ItemStack stack, PlayerEntity player) {
        this.stack = stack;
        this.player = player;
        reIndex();
    }

    @Override
    public Multiset<IUniqueObject<?>> insert(Multiset<IUniqueObject<?>> items, boolean simulate) {
        Multiset<IUniqueObject<?>> copy = HashMultiset.create(items);
        Multiset<IUniqueObject<?>> toRemove = HashMultiset.create();
        for (Multiset.Entry<IUniqueObject<?>> entry : copy.entrySet()) {
            int remainingCount = insertObject(entry.getElement(), entry.getCount(), simulate);
            if (remainingCount < entry.getCount())
                toRemove.add(entry.getElement(), entry.getCount() - remainingCount);
        }
        Multisets.removeOccurrences(copy, toRemove);

        return copy;
    }

    private int insertObject(IUniqueObject<?> obj, int count, boolean simulate) {
        if (obj.preferStackInsert())
            return obj.tryCreateInsertStack(Collections.unmodifiableMap(handleMap), count)
                    .map(itemStack -> performSimpleInsert(itemStack, count, simulate))
                    .orElseGet(() -> performComplexInsert(obj, count, simulate));
        else {
            // This likely has the same disregard for valid slots as the simple insert does
            int remainingCount = performComplexInsert(obj, count, simulate);
            return remainingCount == 0 ? 0 : obj.tryCreateInsertStack(Collections.unmodifiableMap(handleMap), count)
                    .map(itemStack -> performSimpleInsert(itemStack, count, simulate))
                    .orElse(remainingCount);
        }
    }

    private int performSimpleInsert(ItemStack stack, int count, boolean simulate) {
        int remainingCount = insertIntoProviders(stack, count, simulate);
        if (remainingCount == 0)
            return 0;

// this is extremely buggy and poorly planned out code.
//        insertIntoEmptyHandles(stack, remainingCount, simulate);
//        if (remainingCount == 0)
//            return 0;

        if (! simulate)
            spawnRemainder(stack, remainingCount);

        return 0;
    }

    private int insertIntoProviders(ItemStack stack, int remainingCount, boolean simulate) {
        for (IInsertProvider insertProvider : insertProviders) {
            remainingCount -= insertProvider.insert(stack, remainingCount, simulate);
            if (remainingCount <= 0)
                return 0;
        }
        return remainingCount;
    }

    // todo: fix or rewrite. has many root issues:
    //       uses methods not intended for forge, has the ability to replace stacks, indexes players inventory even though we already handle the players inventory,
    //       doesn't check for a valid slot, ignores the IItemHandler contract. Maybe more
    private int insertIntoEmptyHandles(ItemStack stack, int remainingCount, boolean simulate) {
//        List<IObjectHandle<?>> emptyHandles = handleMap
//                .computeIfAbsent(Item.class, c -> new HashMap<>())
//                .getOrDefault(Items.AIR, ImmutableList.of());
//
//        for (Iterator<IObjectHandle<?>> it = emptyHandles.iterator(); it.hasNext() && remainingCount >= 0; ) {
//            IObjectHandle<?> handle = it.next();
//            UniqueItem item = UniqueItem.ofStack(stack);
//
//            int match = handle.insert(item, remainingCount, simulate);
//            if (match > 0)
//                remainingCount -= match;
//
//            handleMap.get(Item.class)
//                    .computeIfAbsent(item.getIndexObject(), i -> new ArrayList<>())
//                    .add(handle);
//
//            if (remainingCount <= 0)
//                return 0;
//        }
//
//        return remainingCount;
        return 0;
    }

    private void spawnRemainder(ItemStack stack, int remainingCount) {
        while (remainingCount > 0) {
            ItemStack copy = stack.copy();
            copy.setCount(Math.min(remainingCount, copy.getMaxStackSize()));
            remainingCount -= copy.getCount();
            ItemEntity itemEntity = new ItemEntity(player.world, player.getX(), player.getY(), player.getZ(), copy);
            player.world.addEntity(itemEntity);
        }
    }

    private int performComplexInsert(IUniqueObject<?> obj, int count, boolean simulate) {
        int remainingCount = count;
        List<IObjectHandle<?>> handles = handleMap
                .getOrDefault(obj.getIndexClass(), ImmutableMap.of())
                .getOrDefault(obj.getIndexObject(), ImmutableList.of());

        for (Iterator<IObjectHandle<?>> it = handles.iterator(); it.hasNext() && remainingCount >= 0; ) {
            IObjectHandle<?> handle = it.next();
            int match = handle.insert(obj, remainingCount, simulate);
            if (match > 0)
                remainingCount -= match;
            if (handle.shouldCleanup())
                it.remove();
            if (remainingCount <= 0)
                return 0;
        }
        return remainingCount;
    }

    @Override
    public void reIndex() {
        this.handleMap = InventoryHelper.indexMap(stack, player);
        this.insertProviders = InventoryHelper.indexInsertProviders(stack, player);
    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        MatchResult result = null;
        for (ImmutableMultiset<IUniqueObject<?>> multiset : list) {
            result = match(list, multiset, true);
            if (result.isSuccess())
                return MatchResult.success(list, result.getFoundItems(), multiset);
        }
        return result == null ? MatchResult.success(list, ImmutableMultiset.of(), ImmutableMultiset.of()) : evaluateFailingOptionFoundItems(list);
    }

    private MatchResult evaluateFailingOptionFoundItems(MaterialList list) {
        Multiset<IUniqueObject<?>> multiset = HashMultiset.create();
        for (ImmutableMultiset<IUniqueObject<?>> option : list.getItemOptions()) {
            for (Entry<IUniqueObject<?>> entry : option.entrySet()) {
                multiset.setCount(entry.getElement(), Math.max(multiset.count(entry.getElement()), entry.getCount()));
            }
        }
        multiset.addAll(list.getRequiredItems());
        MatchResult result = match(list, multiset, true);
        if (result.isSuccess())
            throw new RuntimeException("This should not be possible! The the content changed between matches?!?");
        Iterator<ImmutableMultiset<IUniqueObject<?>>> it = list.iterator();
        return it.hasNext() ? MatchResult.failure(list, result.getFoundItems(), it.next()) : result;
    }

    private MatchResult match(MaterialList list, Multiset<IUniqueObject<?>> multiset, boolean simulate) {
        ImmutableMultiset.Builder<IUniqueObject<?>> availableBuilder = ImmutableMultiset.builder();
        boolean failure = false;
        for (Entry<IUniqueObject<?>> entry : multiset.entrySet()) {
            int remainingCount = entry.getCount();
            Class<?> indexClass = entry.getElement().getIndexClass();
            List<IObjectHandle<?>> entries = handleMap
                    .getOrDefault(indexClass, ImmutableMap.of())
                    .getOrDefault(entry.getElement().getIndexObject(), ImmutableList.of());
            for (Iterator<IObjectHandle<?>> it = entries.iterator(); it.hasNext() && remainingCount >= 0; ) {
                IObjectHandle<?> handle = it.next();
                int match = handle.match(entry.getElement(), remainingCount, simulate);
                if (match > 0)
                    remainingCount -= match;
                if (handle.shouldCleanup()) {
                    it.remove();
                    if (indexClass == Item.class)  //make it ready for insertion if this is an Item handle
                        handleMap.computeIfAbsent(Item.class, c -> new HashMap<>())
                                .computeIfAbsent(Items.AIR, i -> new ArrayList<>())
                                .add(handle);
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
