package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.Registries;
import com.direwolf20.buildinggadgets.api.materials.inventory.IHandleProvider;
import com.direwolf20.buildinggadgets.api.materials.inventory.IObjectHandle;
import com.direwolf20.buildinggadgets.api.materials.inventory.StackHandlerItemHandle;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

public final class ItemHandlerProvider implements IHandleProvider {
    static void index(IItemHandler handler, Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> indexMap) {
        List<ItemStack> stacks = new ArrayList<>(handler.getSlots());
        //first index all sub-providers
        for (int i = 0; i < handler.getSlots(); ++ i) {
            ItemStack stack = handler.getStackInSlot(i);
            stacks.add(stack); //temporarily store this, as this might be an expensive operation (AE-Systems!)
            if (! stack.isEmpty())
                Registries.HandleProvider.indexCapProvider(stack, indexMap);
        }
        //now append the stacks within this handle
        for (int i = 0; i < handler.getSlots(); ++ i) {
            ItemStack stack = stacks.get(i);
            if (! stack.isEmpty()) {
                indexMap.computeIfAbsent(Item.class, clazz -> new HashMap<>())
                        .computeIfAbsent(stack.getItem(), item -> new ArrayList<>())
                        .add(new StackHandlerItemHandle(handler, i));
            } else {
                indexMap.computeIfAbsent(Item.class, clazz -> new HashMap<>())
                        .computeIfAbsent(Items.AIR, item -> new ArrayList<>())
                        .add(new StackHandlerItemHandle(handler, i));
            }
        }
    }

    @Override
    public boolean index(ICapabilityProvider capProvider, Map<Class<?>, Map<Object, List<IObjectHandle<?>>>> indexMap, Set<Class<?>> indexedClasses) {
        if (indexedClasses.contains(Item.class))
            return false;
        LazyOptional<IItemHandler> cap = capProvider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (! cap.isPresent())
            return false;
        IItemHandler handler = cap.orElseThrow(RuntimeException::new);
        index(handler, indexMap);
        indexedClasses.add(Item.class);
        return true;
    }
}
