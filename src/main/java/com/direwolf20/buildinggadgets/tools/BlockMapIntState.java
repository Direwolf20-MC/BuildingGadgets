package com.direwolf20.buildinggadgets.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;

import java.util.HashMap;
import java.util.Map;

public class BlockMapIntState {

    public Map<Short, IBlockState> IntStateMap;

    public BlockMapIntState() {
        IntStateMap = new HashMap<Short, IBlockState>();
    }

    public Map<Short, IBlockState> getIntStateMap() {
        return IntStateMap;
    }

    public void addToMap(IBlockState mapState) {
        if (findSlot(mapState) == -1) {
            short nextSlot = (short)IntStateMap.size();
            nextSlot++;
            IntStateMap.put(nextSlot, mapState);
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

    public IBlockState getStateFromSlot(Short slot) {
        return IntStateMap.get(slot);
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
}
