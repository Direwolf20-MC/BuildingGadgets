package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public final class UniqueItem {
    public static final UniqueItem AIR = new UniqueItem(Items.AIR, 0);
    private final Item item;
    private final int meta;

    public static UniqueItem readFromNBT(NBTTagCompound compound) {
        return new UniqueItem(Item.getItemById(compound.getInteger("item")), compound.getInteger("meta"));
    }

    @Nonnull
    public static UniqueItem fromBlockState(IBlockState state, EntityPlayer player, BlockPos pos) {
        ItemStack itemStack;
        //if (state.getBlock().canSilkHarvest(player.world, pos, state, player)) {
        //    itemStack = InventoryManipulation.getSilkTouchDrop(state);
        //} else {
        //}
        try {
            itemStack = state.getBlock().getPickBlock(state, null, player.world, pos, player);
        } catch (Exception e) {
            itemStack = InventoryManipulation.getSilkTouchDrop(state);
        }
        if (itemStack.isEmpty()) {
            itemStack = InventoryManipulation.getSilkTouchDrop(state);
        }
        if (!itemStack.isEmpty()) {
            return new UniqueItem(itemStack.getItem(), itemStack.getMetadata());
        }
        return AIR;
        //throw new IllegalArgumentException("A UniqueItem could net be retrieved for the the follwing state (at position " + pos + "): " + state);
    }

    public UniqueItem(Item i, int m) {
        if (i.getRegistryName() == null) {
            throw new IllegalArgumentException("Attempted to create UniqueItem for an not registered Item! This is not possible!");
        }
        item = i;
        meta = m;
    }

    public int getMeta() {
        return meta;
    }

    public Item getItem() {
        return item;
    }

    public boolean equals(UniqueItem uniqueItem) {
        //item.equals will fall back to reference Equality
        return (uniqueItem.item.equals(item) && uniqueItem.meta == meta);
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("item", Item.getIdFromItem(this.getItem()));
        compound.setInteger("meta", this.getMeta());
    }

    @Override
    public int hashCode() {
        assert item.getRegistryName() != null; //An Item without registry name cannot exist in here... Construction would have failed otherwise...
        int result = meta;
        result = 31 * result + item.getRegistryName().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueItem)) return false;

        UniqueItem that = (UniqueItem) o;
        return equals(that);
    }
}
