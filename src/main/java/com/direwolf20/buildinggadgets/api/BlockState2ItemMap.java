package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.google.common.collect.BiMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class BlockState2ItemMap extends BlockState2ShortMap {
    private static final String KEY_ITEM_MAP = "mapIntStack";
    private static final String KEY_STATE_ID = "id";
    private static final String KEY_STATE = "state";
    private final Map<IBlockState, UniqueItem> stateItemMap;

    public BlockState2ItemMap(BiMap<Short, IBlockState> shortStateMap, Map<IBlockState, UniqueItem> stateItemMap) {
        super(shortStateMap);
        this.stateItemMap = new HashMap<>(stateItemMap);
    }

    public BlockState2ItemMap() {
        super();
        stateItemMap = new HashMap<>();
    }

    public Map<IBlockState, UniqueItem> getStateItemMap() {
        return stateItemMap;
    }

    @Nonnull
    public static BlockState2ItemMap readFromNBT(@Nullable NBTTagCompound tagCompound) {
        BlockState2ItemMap mapIntState = new BlockState2ItemMap();
        if (tagCompound == null) return mapIntState;
        mapIntState.readNBT(tagCompound);
        return mapIntState;
    }

    public void addToMap(UniqueItem uniqueItem, IBlockState blockState) {
        addToMap(blockState);
        if (!stateItemMap.containsValue(uniqueItem)) {
            stateItemMap.put(blockState, uniqueItem);
        }
    }

    public UniqueItem getItemForState(IBlockState state) {
        return stateItemMap.get(state);
    }

    @Override
    public void clear() {
        super.clear();
        stateItemMap.clear();
    }

    @Override
    public void writeToNBT(@Nonnull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag(KEY_ITEM_MAP, writeStateItemMapToNBT());
    }

    @Override
    public void readNBT(@Nonnull NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        if (tagCompound.hasKey(KEY_ITEM_MAP)) {
            NBTTagList mapIntStackTag = (NBTTagList) tagCompound.getTag(KEY_ITEM_MAP);
            readStateItemMapFromNBT(mapIntStackTag);
        }
    }

    public void initStateItemMap(EntityPlayer player) {
        stateItemMap.clear();
        for (Map.Entry<Short, IBlockState> entry : getShortStateMap().entrySet()) {
            try {
                stateItemMap.put(entry.getValue(), UniqueItem.fromBlockState(entry.getValue(), player, new BlockPos(0, 0, 0)));
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private NBTTagList writeStateItemMapToNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<IBlockState, UniqueItem> entry : stateItemMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            entry.getValue().writeToNBT(compound);
            short slot = getSlot(entry.getKey());
            if (slot>=0)
                compound.setShort(KEY_STATE_ID,slot);
            else
                compound.setTag(KEY_STATE,GadgetUtils.stateToCompound(entry.getKey()));
            tagList.appendTag(compound);
        }
        return tagList;
    }

    private void readStateItemMapFromNBT(NBTTagList tagList) {
        stateItemMap.clear();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            UniqueItem item = UniqueItem.readFromNBT(compound);
            IBlockState state = null;
            if (compound.hasKey(KEY_STATE_ID))
                state = getStateFromSlot(compound.getShort(KEY_STATE_ID));
            if (state == null)
                state = GadgetUtils.compoundToState(compound.getCompoundTag(KEY_STATE));
            stateItemMap.put(state, item);
        }
    }
}
