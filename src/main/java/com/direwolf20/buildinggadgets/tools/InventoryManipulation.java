package com.direwolf20.buildinggadgets.tools;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InventoryManipulation {
    private static final Set<IProperty> safeProperties = Stream.of(BlockSlab.HALF, BlockStairs.HALF, BlockLog.LOG_AXIS,
            BlockDirectional.FACING, BlockStairs.FACING, BlockTrapDoor.HALF).collect(Collectors.toSet());

    public static boolean giveItem(ItemStack itemStack, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        InventoryPlayer inv = player.inventory;
        ItemStack giveItemStack = itemStack.copy();
        boolean success = inv.addItemStackToInventory(giveItemStack);
        return success;
    }

    public static boolean useItem(ItemStack itemStack, EntityPlayer player, int count) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }
        InventoryPlayer inv = player.inventory;

        ArrayList<Integer> slots = findItem(itemStack.getItem(), itemStack.getMetadata(), inv);
        if (slots.size() == 0) {
            return false;
        }
        int slot = slots.get(0);
        ItemStack stackInSlot = inv.getStackInSlot(slot);
        if (stackInSlot.getCount() < count) {
            return false;
        }
        stackInSlot.shrink(count);
        stackInSlot = stackInSlot;
        return true;
    }

    public static int countItem(ItemStack itemStack, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            return 10000;
        }
        int count = 0;
        InventoryPlayer inv = player.inventory;
        ArrayList<Integer> slots = findItem(itemStack.getItem(), itemStack.getMetadata(), inv);
        if (slots.size() == 0) {
            return 0;
        }
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

    public static ItemStack getSilkTouchDrop(IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        int i = 0;
        if (item.getHasSubtypes()) {
            i = state.getBlock().damageDropped(state);
        }
        return new ItemStack(item, 1, i);
    }

    public static IBlockState getSpecificStates(IBlockState originalState, World world, EntityPlayer player) {
        IBlockState placeState = Blocks.AIR.getDefaultState();
        try {
            placeState = originalState.getBlock().getStateForPlacement(world, new BlockPos(0,0,0),EnumFacing.UP, 0,0,0,originalState.getBlock().getMetaFromState(originalState),player,EnumHand.MAIN_HAND);
        } catch (Exception var8) {
            placeState = originalState.getBlock().getDefaultState();
        }
        for (IProperty prop : placeState.getPropertyKeys()) {
            if (safeProperties.contains(prop)) {
                placeState = placeState.withProperty(prop, originalState.getValue(prop));
            }
        }
        return placeState;

    }
}
