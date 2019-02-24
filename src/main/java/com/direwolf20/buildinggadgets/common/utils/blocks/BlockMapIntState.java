package com.direwolf20.buildinggadgets.common.utils.blocks;

import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.utils.helpers.InventoryHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Deprecated //TODO @since 1.13.x replace with BlockState2ItemMap and BlockState2ShortMap respectively
public class BlockMapIntState {

    public Map<Short, IBlockState> intStateMap = new HashMap<>();
    public Map<IBlockState, UniqueItem> intStackMap = new HashMap<>();

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

    @Nullable
    private IBlockState findStackSlot(UniqueItem uniqueItem) {
        for (Map.Entry<IBlockState, UniqueItem> entry : intStackMap.entrySet()) {
            if (entry.getValue().getItem() == uniqueItem.getItem()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public IBlockState getStateFromSlot(Short slot) {
        return intStateMap.get(slot);
    }

    public UniqueItem getStackFromSlot(IBlockState blockState) {//TODO unused
        return intStackMap.get(blockState);
    }

    public Map<Short, IBlockState> getIntStateMapFromNBT(NBTTagList tagList) {
        intStateMap = new HashMap<>();
        for (int i = 0; i < tagList.size(); i++) {
            NBTTagCompound compound = tagList.getCompound(i);
            intStateMap.put(compound.getShort("mapSlot"), NBTUtil.readBlockState(compound.getCompound("mapState")));
        }
        return intStateMap;
    }

    public NBTTagList putIntStateMapIntoNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<Short, IBlockState> entry : intStateMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setShort("mapSlot", entry.getKey());
            compound.setTag("mapState", NBTUtil.writeBlockState(entry.getValue()));
            tagList.add(compound);
        }
        return tagList;
    }

    public Map<IBlockState, UniqueItem> getIntStackMapFromNBT(NBTTagList tagList) {
        intStackMap = new HashMap<>();
        for (int i = 0; i < tagList.size(); i++) {
            NBTTagCompound compound = tagList.getCompound(i);
            intStackMap.put(GadgetUtils.compoundToState(compound.getCompound("state")), UniqueItem.readFromNBT(compound));
        }
        return intStackMap;
    }

    public NBTTagList putIntStackMapIntoNBT() {
        NBTTagList tagList = new NBTTagList();
        for (Map.Entry<IBlockState, UniqueItem> entry : intStackMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            entry.getValue().writeToNBT(compound);
            compound.setTag("state", GadgetUtils.stateToCompound(entry.getKey()));
            tagList.add(compound);
        }
        return tagList;
    }

    @Nonnull
    public static UniqueItem blockStateToUniqueItem(IBlockState state, EntityPlayer player, BlockPos pos) {
        ItemStack itemStack;

        try {
            itemStack = state.getBlock().getPickBlock(state, null, player.world, pos, player);
        } catch (Exception e) {
            itemStack = InventoryHelper.getSilkTouchDrop(state);
        }
        if (itemStack.isEmpty()) {
            itemStack = InventoryHelper.getSilkTouchDrop(state);
        }
        if (!itemStack.isEmpty()) {
            return new UniqueItem(itemStack.getItem());
        }
        return new UniqueItem(Items.AIR);
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
