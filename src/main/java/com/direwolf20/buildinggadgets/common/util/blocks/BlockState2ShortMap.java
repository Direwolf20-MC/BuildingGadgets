package com.direwolf20.buildinggadgets.common.util.blocks;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class BlockState2ShortMap {
    private final BiMap<Short, BlockState> shortStateMap;

    public BlockState2ShortMap(BiMap<Short, BlockState> shortStateMap) {
        this.shortStateMap = shortStateMap;
    }

    public BlockState2ShortMap() {
        this(HashBiMap.create());
    }

    @Nonnull
    public static BlockState2ShortMap readFromNBT(@Nullable CompoundNBT tagCompound) {
        BlockState2ShortMap mapIntState = new BlockState2ShortMap();
        if (tagCompound == null) return mapIntState;
        mapIntState.readNBT(tagCompound);
        return mapIntState;
    }

    protected BiMap<Short, BlockState> getShortStateMap() {
        return shortStateMap;
    }

    public void addToMap(BlockState mapState) {
        if (!shortStateMap.containsValue(mapState)) {
            //this adds the mapState to max Slot - starting at 0, not at 1 as before
            shortStateMap.put((short) (shortStateMap.size()), mapState);
        }
    }

    public short getSlot(BlockState mapState) {
        Short res = shortStateMap.inverse().get(mapState);
        return res != null ? res : -1;
    }

    public BlockState getStateFromSlot(short slot) {
        return shortStateMap.get(slot);
    }

    public void writeToNBT(@Nonnull CompoundNBT tagCompound) {
        tagCompound.put(NBTKeys.MAP_PALETTE, writeShortStateMapToNBT());
    }

    public void readNBT(@Nonnull CompoundNBT tagCompound) {
        clear();
        if (tagCompound.contains(NBTKeys.MAP_PALETTE)) {
            // fixme: use getList?
            ListNBT mapIntStateTag = (ListNBT) tagCompound.get(NBTKeys.MAP_PALETTE);
            readShortStateMapFromNBT(mapIntStateTag);
        }
    }

    public void clear() {
        shortStateMap.clear();
    }

    protected ListNBT writeShortStateMapToNBT() {
        ListNBT tagList = new ListNBT();
        for (Map.Entry<Short, BlockState> entry : shortStateMap.entrySet()) {
            CompoundNBT compound = new CompoundNBT();
            CompoundNBT state = NBTUtil.writeBlockState(entry.getValue());
            compound.putShort(NBTKeys.MAP_SLOT, entry.getKey());
            compound.put(NBTKeys.MAP_STATE, state);
            tagList.add(compound);
        }
        return tagList;
    }

    private void readShortStateMapFromNBT(ListNBT tagList) {
        shortStateMap.clear();
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT compound = tagList.getCompound(i);
            shortStateMap.put(compound.getShort(NBTKeys.MAP_SLOT), NBTUtil.readBlockState(compound.getCompound(NBTKeys.MAP_STATE)));
        }
    }
}
