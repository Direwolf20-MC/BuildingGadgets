package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.api.UniqueItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockMapIntState {

    public Map<Short, IBlockState> intStateMap;
    public Map<IBlockState, UniqueItem> intStackMap;

    public BlockMapIntState() {
        intStateMap = new HashMap<Short, IBlockState>();
        intStackMap = new HashMap<IBlockState, UniqueItem>();
    }

    public Map<Short, IBlockState> getIntStateMap() {
        return intStateMap;
    }

    public Map<IBlockState, UniqueItem> getIntStackMap() {
        return intStackMap;
    }

    public void addToMap(IBlockState mapState) {
        if (findSlot(mapState) == -1) {
            short nextSlot = (short) intStateMap.size();
            nextSlot++;
            intStateMap.put(nextSlot, mapState);
        }
    }

    public void addToStackMap(UniqueItem uniqueItem, IBlockState blockState) {
        if (findStackSlot(uniqueItem) != blockState) {
            intStackMap.put(blockState, uniqueItem);
        }
    }

    public Short findSlot(IBlockState mapState) {
        for (Map.Entry<Short, IBlockState> entry : intStateMap.entrySet()) {
            if (entry.getValue() == mapState) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public NBTTagList putIntStackMapIntoNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<IBlockState, UniqueItem> entry : intStackMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("item", Item.getIdFromItem(entry.getValue().getItem()));
            compound.setInteger("meta", entry.getValue().getMeta());
            compound.setTag("state", GadgetUtils.stateToCompound(entry.getKey()));
            tagList.appendTag(compound);
        }
        return tagList;
    }

    public IBlockState getStateFromSlot(Short slot) {
        return intStateMap.get(slot);
    }

    public UniqueItem getStackFromSlot(IBlockState blockState) {//TODO unused
        return intStackMap.get(blockState);
    }

    public Map<Short, IBlockState> getIntStateMapFromNBT(NBTTagList tagList) {
        intStateMap = new HashMap<Short, IBlockState>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            intStateMap.put(compound.getShort("mapSlot"), NBTUtil.readBlockState(compound.getCompoundTag("mapState")));
        }
        return intStateMap;
    }

    public NBTTagList putIntStateMapIntoNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<Short, IBlockState> entry : intStateMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagCompound state = new NBTTagCompound();
            NBTUtil.writeBlockState(state, entry.getValue());
            compound.setShort("mapSlot", entry.getKey());
            compound.setTag("mapState", state);
            tagList.appendTag(compound);
        }
        return tagList;
    }

    public Map<IBlockState, UniqueItem> getIntStackMapFromNBT(NBTTagList tagList) {
        intStackMap = new HashMap<IBlockState, UniqueItem>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            intStackMap.put(GadgetUtils.compoundToState(compound.getCompoundTag("state")), new UniqueItem(Item.getItemById(compound.getInteger("item")), compound.getInteger("meta")));
        }
        return intStackMap;
    }

    @Nullable
    private IBlockState findStackSlot(UniqueItem uniqueItem) {
        for (Map.Entry<IBlockState, UniqueItem> entry : intStackMap.entrySet()) {
            if (entry.getValue().getItem() == uniqueItem.getItem() && entry.getValue().getMeta() == uniqueItem.getMeta()) { //TODO this can propably be replaced with equality check
                return entry.getKey();
            }
        }
        return null;
    }

    @Nonnull
    public static UniqueItem blockStateToUniqueItem(IBlockState state, EntityPlayer player, BlockPos pos) {
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
            UniqueItem uniqueItem = new UniqueItem(itemStack.getItem(), itemStack.getMetadata());
            return uniqueItem;
        }
        UniqueItem uniqueItem = new UniqueItem(Items.AIR, 0);
        return uniqueItem;
        //throw new IllegalArgumentException("A UniqueItem could net be retrieved for the the follwing state (at position " + pos + "): " + state);
    }

    public void makeStackMapFromStateMap(EntityPlayer player) {
        intStackMap.clear();
        for (Map.Entry<Short, IBlockState> entry : intStateMap.entrySet()) {
            try {
                intStackMap.put(entry.getValue(), blockStateToUniqueItem(entry.getValue(), player, new BlockPos(0, 0, 0)));
            } catch (IllegalArgumentException e) {
            }
        }
    }
}
