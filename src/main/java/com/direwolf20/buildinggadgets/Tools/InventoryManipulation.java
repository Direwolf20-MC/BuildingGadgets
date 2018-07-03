package com.direwolf20.buildinggadgets.Tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Set;

public class InventoryManipulation {

    public static boolean giveItem(ItemStack itemStack, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        InventoryPlayer inv = player.inventory;
        ItemStack giveItemStack = itemStack.copy();
        boolean success = inv.addItemStackToInventory(giveItemStack);
        return success;
    }

    public static boolean useItem(ItemStack itemStack, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        InventoryPlayer inv = player.inventory;
        //int slot = inv.getSlotFor(itemStack);
        //if (slot == -1) {return false;}

        ArrayList<Integer> slots = findItem(itemStack.getItem(),itemStack.getMetadata(),inv);
        if (slots.size() == 0) {return false;}
        int slot = slots.get(0);
        ItemStack stackInSlot = inv.getStackInSlot(slot);
        stackInSlot.shrink(1);
        stackInSlot = stackInSlot;
        if (stackInSlot.getCount() == 0) {
            //inv.setInventorySlotContents(slot, ItemStack.EMPTY);
        }

        return true;
    }
    public static int countItem(ItemStack itemStack,EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return 10000;
        }
        int count=0;
        InventoryPlayer inv = player.inventory;
        ArrayList<Integer> slots = findItem(itemStack.getItem(),itemStack.getMetadata(),inv);
        if (slots.size() == 0) {return 0;}
        for (int slot : slots) {
            ItemStack stackInSlot = inv.getStackInSlot(slot);
            count += stackInSlot.getCount();
        }
        return count;
    }

    public static ArrayList<Integer> findItem(Item item, int meta, InventoryPlayer inv) {
        ArrayList<Integer> slots = new ArrayList<Integer>();
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item && meta == stack.getItemDamage()) {
                slots.add(i);
            }
        }

        return slots;
    }
}
