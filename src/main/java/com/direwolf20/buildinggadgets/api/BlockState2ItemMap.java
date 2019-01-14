package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
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
    private final Map<IBlockState, UniqueItem> stateItemMap;

    public BlockState2ItemMap() {
        stateItemMap = new HashMap<>();
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
    public void writeToNBT(@Nonnull NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setTag("mapIntStack", writeStateItemMapToNBT());
    }

    @Override
    public void readNBT(@Nonnull NBTTagCompound tagCompound) {
        super.readNBT(tagCompound);
        NBTTagList mapIntStackTag = (NBTTagList) tagCompound.getTag("mapIntStack");
        readStateItemMapFromNBT(mapIntStackTag == null ? mapIntStackTag : new NBTTagList());
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
            compound.setTag("state", GadgetUtils.stateToCompound(entry.getKey()));
            tagList.appendTag(compound);
        }
        return tagList;
    }

    private void readStateItemMapFromNBT(NBTTagList tagList) {
        stateItemMap.clear();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            stateItemMap.put(GadgetUtils.compoundToState(compound.getCompoundTag("state")), UniqueItem.readFromNBT(compound));
        }
    }
}
