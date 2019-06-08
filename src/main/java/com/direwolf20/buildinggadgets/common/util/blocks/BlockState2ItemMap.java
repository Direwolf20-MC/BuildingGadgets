package com.direwolf20.buildinggadgets.common.util.blocks;

import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.BiMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class BlockState2ItemMap extends BlockState2ShortMap {
    private final Map<BlockState, UniqueItem> stateItemMap;

    public BlockState2ItemMap(BiMap<Short, BlockState> shortStateMap, Map<BlockState, UniqueItem> stateItemMap) {
        super(shortStateMap);
        this.stateItemMap = new HashMap<>(stateItemMap);
    }

    public BlockState2ItemMap() {
        super();
        stateItemMap = new HashMap<>();
    }

    public Map<BlockState, UniqueItem> getStateItemMap() {
        return stateItemMap;
    }

    @Nonnull
    public static BlockState2ItemMap readFromNBT(@Nullable CompoundNBT tagCompound) {
        BlockState2ItemMap mapIntState = new BlockState2ItemMap();
        if (tagCompound == null) return mapIntState;
        mapIntState.readNBT(tagCompound);
        return mapIntState;
    }

    public void addToMap(UniqueItem uniqueItem, BlockState blockState) {
        addToMap(blockState);
        if (!stateItemMap.containsValue(uniqueItem)) {
            stateItemMap.put(blockState, uniqueItem);
        }
    }

    public UniqueItem getItemForState(BlockState state) {
        return stateItemMap.get(state);
    }

    @Override
    public void clear() {
        super.clear();
        stateItemMap.clear();
    }

    @Override
    public void writeToNBT(@Nonnull CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.put(NBTKeys.MAP_INT_STACK, writeStateItemMapToNBT());
    }

    @Override
    public void readNBT(@Nonnull CompoundNBT tagCompound) {
        super.readNBT(tagCompound);
        if (tagCompound.contains(NBTKeys.MAP_INT_STACK)) {
            // fixme: use getList?
            ListNBT mapIntStackTag = (ListNBT) tagCompound.get(NBTKeys.MAP_INT_STACK);
            readStateItemMapFromNBT(mapIntStackTag);
        }
    }

    public void initStateItemMap(ClientPlayerEntity player) {
        stateItemMap.clear();
        for (Map.Entry<Short, BlockState> entry : getShortStateMap().entrySet()) {
            try {
                stateItemMap.put(entry.getValue(), UniqueItem.fromBlockState(entry.getValue(), player, new BlockPos(0, 0, 0)));
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private ListNBT writeStateItemMapToNBT() {
        ListNBT tagList = new ListNBT();
        for (Map.Entry<BlockState, UniqueItem> entry : stateItemMap.entrySet()) {
            CompoundNBT compound = new CompoundNBT();
            entry.getValue().writeToNBT(compound);
            short slot = getSlot(entry.getKey());
            if (slot >= 0) {
                compound.putShort(NBTKeys.MAP_STATE_ID, slot);
            } else {
                compound.put(NBTKeys.MAP_STATE, GadgetUtils.stateToCompound(entry.getKey()));
            }
            tagList.add(compound);
        }
        return tagList;
    }

    private void readStateItemMapFromNBT(ListNBT tagList) {
        stateItemMap.clear();
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT compound = tagList.getCompound(i);
            UniqueItem item = UniqueItem.readFromNBT(compound);
            BlockState state = null;
            if (compound.contains(NBTKeys.MAP_STATE_ID)) {
                state = getStateFromSlot(compound.getShort(NBTKeys.MAP_STATE_ID));
            }
            if (state == null) {
                state = GadgetUtils.compoundToState(compound.getCompound(NBTKeys.MAP_STATE));
            }
            stateItemMap.put(state, item);
        }
    }
}