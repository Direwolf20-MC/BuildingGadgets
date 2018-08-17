package com.direwolf20.buildinggadgets.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;

import java.util.HashMap;
import java.util.Map;

public class BlockMapIntState {

    public Map<Short, IBlockState> IntStateMap;
    public Map<IBlockState, UniqueItem> IntStackMap;

    public BlockMapIntState() {
        IntStateMap = new HashMap<Short, IBlockState>();
        IntStackMap = new HashMap<IBlockState, UniqueItem>();
    }

    public Map<Short, IBlockState> getIntStateMap() {
        return IntStateMap;
    }

    public Map<IBlockState, UniqueItem> getIntStackMap() {
        return IntStackMap;
    }

    public void addToMap(IBlockState mapState) {
        if (findSlot(mapState) == -1) {
            short nextSlot = (short)IntStateMap.size();
            nextSlot++;
            IntStateMap.put(nextSlot, mapState);
        }
    }

    public void addToStackMap(UniqueItem uniqueItem, IBlockState blockState) {
        if (findStackSlot(uniqueItem) != blockState) {
            IntStackMap.put(blockState, uniqueItem);
        }
    }

    public Short findSlot(IBlockState mapState) {
        for (Map.Entry<Short, IBlockState> entry : IntStateMap.entrySet()) {
            if (entry.getValue() == mapState) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public IBlockState findStackSlot(UniqueItem uniqueItem) {
        for (Map.Entry<IBlockState, UniqueItem> entry : IntStackMap.entrySet()) {
            if (entry.getValue().item == uniqueItem.item && entry.getValue().meta == uniqueItem.meta) {
                return entry.getKey();
            }
        }
        return null;
    }

    public IBlockState getStateFromSlot(Short slot) {
        return IntStateMap.get(slot);
    }

    public UniqueItem getStackFromSlot(IBlockState blockState) {
        return IntStackMap.get(blockState);
    }

    public Map<Short, IBlockState> getIntStateMapFromNBT(NBTTagList tagList) {
        IntStateMap = new HashMap<Short, IBlockState>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            IntStateMap.put(compound.getShort("mapSlot"), NBTUtil.readBlockState(compound.getCompoundTag("mapState")));
        }
        return IntStateMap;
    }

    public NBTTagList putIntStateMapIntoNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<Short, IBlockState> entry : IntStateMap.entrySet()) {
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
        IntStackMap = new HashMap<IBlockState, UniqueItem>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            IntStackMap.put(GadgetUtils.compoundToState(compound.getCompoundTag("state")), new UniqueItem(Item.getItemById(compound.getInteger("item")), compound.getInteger("meta")));
        }
        return IntStackMap;
    }

    public NBTTagList putIntStackMapIntoNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<IBlockState, UniqueItem> entry : IntStackMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("item", Item.getIdFromItem(entry.getValue().item));
            compound.setInteger("meta", entry.getValue().meta);
            compound.setTag("state", GadgetUtils.stateToCompound(entry.getKey()));
            tagList.appendTag(compound);
        }
        return tagList;
    }
}
