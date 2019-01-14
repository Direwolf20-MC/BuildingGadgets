package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface ITemplateOld {

    @Nullable
    String getUUID(ItemStack stack);

    WorldSave getWorldSave(World world);

    default void setItemCountMap(ItemStack stack, Map<UniqueItem, Integer> tagMap) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList tagList = GadgetUtils.itemCountToNBT(tagMap);
        tagCompound.setTag("itemcountmap", tagList);
        stack.setTagCompound(tagCompound);
    }

    @Nonnull
    default Map<UniqueItem, Integer> getItemCountMap(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        Map<UniqueItem, Integer> tagMap = tagCompound == null ? null : GadgetUtils.nbtToItemCount((NBTTagList) tagCompound.getTag("itemcountmap"));
        if (tagMap == null)
            throw new IllegalArgumentException("ITemplate#getItemCountMap faild to retieve tag map from " + GadgetUtils.getStackErrorSuffix(stack));

        return tagMap;
    }

    default int getCopyCounter(ItemStack stack) {
        return GadgetUtils.getStackTag(stack).getInteger("copycounter");
    }

    default void incrementCopyCounter(ItemStack stack) {
        NBTTagCompound tagCompound = GadgetUtils.getStackTag(stack);
        tagCompound.setInteger("copycounter", tagCompound.getInteger("copycounter") + 1);
        stack.setTagCompound(tagCompound);
    }

    default void setStartPos(ItemStack stack, BlockPos startPos) {
        GadgetUtils.writePOSToNBT(stack, startPos, "startPos");
    }

    @Nullable
    default BlockPos getStartPos(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, "startPos");
    }

    default void setEndPos(ItemStack stack, BlockPos startPos) {
        GadgetUtils.writePOSToNBT(stack, startPos, "endPos");
    }

    @Nullable
    default BlockPos getEndPos(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, "endPos");
    }

}
