package com.direwolf20.buildinggadgets.common.util.blocks;

import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Deprecated //TODO @since 1.13.x replace with BlockState2ItemMap and BlockState2ShortMap respectively
public class BlockMapIntState {

    public Map<Short, BlockState> intStateMap = new HashMap<>();
    public Map<BlockState, UniqueItem> intStackMap = new HashMap<>();

    public Map<BlockState, UniqueItem> getIntStackMap() {
        return intStackMap;
    }

    public void addToMap(BlockState mapState) {
        if (findSlot(mapState) == -1) {
            short nextSlot = (short) intStateMap.size();
            nextSlot++;
            intStateMap.put(nextSlot, mapState);
        }
    }

    public void addToStackMap(UniqueItem uniqueItem, BlockState blockState) {
        if (findStackSlot(uniqueItem) != blockState) {
            intStackMap.put(blockState, uniqueItem);
        }
    }

    public Short findSlot(BlockState mapState) {
        for (Map.Entry<Short, BlockState> entry : intStateMap.entrySet()) {
            if (entry.getValue() == mapState) {
                return entry.getKey();
            }
        }
        return -1;
    }

    @Nullable
    private BlockState findStackSlot(UniqueItem uniqueItem) {
        for (Map.Entry<BlockState, UniqueItem> entry : intStackMap.entrySet()) {
            if (entry.getValue().getItem() == uniqueItem.getItem()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public BlockState getStateFromSlot(Short slot) {
        return intStateMap.get(slot);
    }

    public UniqueItem getStackFromSlot(BlockState blockState) {//TODO unused
        return intStackMap.get(blockState);
    }

    public Map<Short, BlockState> getIntStateMapFromNBT(ListNBT tagList) {
        intStateMap = new HashMap<>();
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT compound = tagList.getCompound(i);
            intStateMap.put(compound.getShort(NBTKeys.MAP_SLOT), NBTUtil.readBlockState(compound.getCompound(NBTKeys.MAP_STATE)));
        }
        return intStateMap;
    }

    public ListNBT putIntStateMapIntoNBT() {
        ListNBT tagList = new ListNBT();
        for (Map.Entry<Short, BlockState> entry : intStateMap.entrySet()) {
            CompoundNBT compound = new CompoundNBT();
            compound.putShort(NBTKeys.MAP_SLOT, entry.getKey());
            compound.put(NBTKeys.MAP_STATE, NBTUtil.writeBlockState(entry.getValue()));
            tagList.add(compound);
        }
        return tagList;
    }

    public Map<BlockState, UniqueItem> getIntStackMapFromNBT(ListNBT tagList) {
        intStackMap = new HashMap<>();
        for (int i = 0; i < tagList.size(); i++) {
            CompoundNBT compound = tagList.getCompound(i);
            intStackMap.put(GadgetUtils.compoundToState(compound.getCompound(NBTKeys.MAP_STATE)), UniqueItem.readFromNBT(compound));
        }
        return intStackMap;
    }

    public ListNBT putIntStackMapIntoNBT() {
        ListNBT tagList = new ListNBT();
        for (Map.Entry<BlockState, UniqueItem> entry : intStackMap.entrySet()) {
            CompoundNBT compound = new CompoundNBT();
            entry.getValue().writeToNBT(compound);
            compound.put(NBTKeys.MAP_STATE, GadgetUtils.stateToCompound(entry.getKey()));
            tagList.add(compound);
        }
        return tagList;
    }

    @Nonnull
    public static UniqueItem blockStateToUniqueItem(BlockState state, PlayerEntity player, BlockPos pos) {
        return UniqueItem.fromBlockState(state, player, pos);
    }

    public void makeStackMapFromStateMap(PlayerEntity player) {
        intStackMap.clear();
        for (Map.Entry<Short, BlockState> entry : intStateMap.entrySet()) {
            try {
                intStackMap.put(entry.getValue(), blockStateToUniqueItem(entry.getValue(), player, new BlockPos(0, 0, 0)));
            } catch (IllegalArgumentException e) {
            }
        }
    }
}
