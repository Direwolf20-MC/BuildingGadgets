package com.direwolf20.buildinggadgets.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class BlockState2ShortMap {
    private final BiMap<Short, IBlockState> shortStateMap;

    public BlockState2ShortMap() {
        shortStateMap = HashBiMap.create();
    }

    @Nonnull
    public static BlockState2ShortMap readFromNBT(@Nullable NBTTagCompound tagCompound) {
        BlockState2ItemMap mapIntState = new BlockState2ItemMap();
        if (tagCompound == null) return mapIntState;
        return mapIntState;
    }

    protected BiMap<Short, IBlockState> getShortStateMap() {
        return shortStateMap;
    }

    public void addToMap(IBlockState mapState) {
        if (!shortStateMap.containsValue(mapState)) {
            //this adds the mapState to max Slot - starting at 0, not at 1 as before
            shortStateMap.put((short) (shortStateMap.size()), mapState);
        }
    }

    public short getSlot(IBlockState mapState) {
        Short res = shortStateMap.inverse().get(mapState);
        return res != null ? res : -1;
    }

    public IBlockState getStateFromSlot(short slot) {
        return shortStateMap.get(slot);
    }

    public void writeToNBT(@Nonnull NBTTagCompound tagCompound) {
        tagCompound.setTag("mapIntState", writeShortStateMapToNBT());
    }

    public void readNBT(@Nonnull NBTTagCompound tagCompound) {
        NBTTagList mapIntStateTag = (NBTTagList) tagCompound.getTag("mapIntState");
        readShortStateMapFromNBT(mapIntStateTag == null ? new NBTTagList() : mapIntStateTag);
    }

    protected NBTTagList writeShortStateMapToNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<Short, IBlockState> entry : shortStateMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagCompound state = new NBTTagCompound();
            NBTUtil.writeBlockState(state, entry.getValue());
            compound.setShort("mapSlot", entry.getKey());
            compound.setTag("mapState", state);
            tagList.appendTag(compound);
        }
        return tagList;
    }

    private void readShortStateMapFromNBT(NBTTagList tagList) {
        shortStateMap.clear();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            shortStateMap.put(compound.getShort("mapSlot"), NBTUtil.readBlockState(compound.getCompoundTag("mapState")));
        }
    }
}
