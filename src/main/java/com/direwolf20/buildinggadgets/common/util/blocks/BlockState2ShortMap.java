package com.direwolf20.buildinggadgets.common.util.blocks;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
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

    public BlockState2ShortMap(BiMap<Short, IBlockState> shortStateMap) {
        this.shortStateMap = shortStateMap;
    }

    public BlockState2ShortMap() {
        this(HashBiMap.create());
    }

    @Nonnull
    public static BlockState2ShortMap readFromNBT(@Nullable NBTTagCompound tagCompound) {
        BlockState2ShortMap mapIntState = new BlockState2ShortMap();
        if (tagCompound == null) return mapIntState;
        mapIntState.readNBT(tagCompound);
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
        tagCompound.setTag(NBTKeys.MAP_PALETTE, writeShortStateMapToNBT());
    }

    public void readNBT(@Nonnull NBTTagCompound tagCompound) {
        clear();
        if (tagCompound.hasKey(NBTKeys.MAP_PALETTE)) {
            NBTTagList mapIntStateTag = (NBTTagList) tagCompound.getTag(NBTKeys.MAP_PALETTE);
            readShortStateMapFromNBT(mapIntStateTag);
        }
    }

    public void clear() {
        shortStateMap.clear();
    }

    protected NBTTagList writeShortStateMapToNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<Short, IBlockState> entry : shortStateMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagCompound state = NBTUtil.writeBlockState(entry.getValue());
            compound.setShort(NBTKeys.MAP_SLOT, entry.getKey());
            compound.setTag(NBTKeys.MAP_STATE, state);
            tagList.add(compound);
        }
        return tagList;
    }

    private void readShortStateMapFromNBT(NBTTagList tagList) {
        shortStateMap.clear();
        for (int i = 0; i < tagList.size(); i++) {
            NBTTagCompound compound = tagList.getCompound(i);
            shortStateMap.put(compound.getShort(NBTKeys.MAP_SLOT), NBTUtil.readBlockState(compound.getCompound(NBTKeys.MAP_STATE)));
        }
    }
}
