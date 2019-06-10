package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.block.BlockState;
import PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public final class UniqueItem { //TODO @since 1.13.x can this be replaced with Item?
    public static final UniqueItem AIR = new UniqueItem(Items.AIR);
    private final Item item;

    public static UniqueItem readFromNBT(CompoundNBT compound) {
        return new UniqueItem(ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString(NBTKeys.UNIQUE_ITEM_ITEM))));
    }

    public static UniqueItem fromBlockState(BlockState state, PlayerEntity player, BlockPos pos) {
        ItemStack itemStack;
        //if (state.getBlock().canSilkHarvest(player.world, pos, state, player)) {
        //    itemStack = InventoryHelper.getSilkTouchDrop(state);
        //} else {
        //}
        try {
            itemStack = state.getBlock().getPickBlock(state, null, player.world, pos, player);
        } catch (Exception e) {
            itemStack = InventoryHelper.getSilkTouchDrop(state);
        }
        if (itemStack.isEmpty()) {
            itemStack = InventoryHelper.getSilkTouchDrop(state);
        }
        if (!itemStack.isEmpty()) {
            return new UniqueItem(itemStack.getItem());
        }
        return AIR;
        //throw new IllegalArgumentException("A UniqueItem could net be retrieved for the the follwing state (at position " + pos + "): " + state);
    }

    public UniqueItem(Item i) {
        Preconditions.checkArgument(Objects.requireNonNull(i).getRegistryName() != null,
                "Attempted to create UniqueItem for an not registered Item! This is not possible!");
        item = i;
    }

    public Item getItem() {
        return item;
    }

    public ItemStack toItemStack() {
        return new ItemStack(getItem());
    }

    public boolean equals(UniqueItem uniqueItem) {
        //item.equals will fall back to reference Equality
        return (uniqueItem.item.equals(item));
    }

    public void writeToNBT(CompoundNBT compound) {
        assert item.getRegistryName() != null; //An Item without registry name cannot exist in here... Construction would have failed otherwise...
        compound.putString(NBTKeys.UNIQUE_ITEM_ITEM, this.getItem().getRegistryName().toString());
    }

    @Override
    public int hashCode() {
        assert item.getRegistryName() != null; //An Item without registry name cannot exist in here... Construction would have failed otherwise...
        return item.getRegistryName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return !(o instanceof UniqueItem) ? false : equals((UniqueItem) o);
    }
}
