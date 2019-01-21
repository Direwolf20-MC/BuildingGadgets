package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.NBTTool;
import com.google.common.collect.Multiset;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface ITemplateOld {
    public static final String KEY_UUID = "UUID";
    public static final UUID INVALID_UUID = new UUID(0,0);

    public default UUID getUUID(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        UUID uuid = tagCompound.getUniqueId(KEY_UUID);
        if (uuid.equals(INVALID_UUID)) {
            String id = tagCompound.getString(KEY_UUID);
            if (!id.isEmpty()) {
                uuid = UUID.fromString(id);
                setUUID(uuid,tagCompound);
                stack.setTagCompound(tagCompound);
            }
        }
        if (uuid == null || uuid.equals(INVALID_UUID)) {
            uuid = UUID.randomUUID();
            setUUID(uuid,tagCompound);
            stack.setTagCompound(tagCompound);
        }
        return uuid;
    }

    public static void setUUID (UUID id, NBTTagCompound tagCompound) {
        tagCompound.setUniqueId(KEY_UUID, id);
    }

    public default WorldSave getWorldSave(World world) {
        return WorldSave.getTemplateWorldSave(world);
    }

    default void setItemCountMap(ItemStack stack, Multiset<UniqueItem> tagMap) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList tagList = NBTTool.itemCountToNBT(tagMap);
        tagCompound.setTag("itemcountmap", tagList);
        stack.setTagCompound(tagCompound);
    }

    @Nonnull
    default Multiset<UniqueItem> getItemCountMap(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        Multiset<UniqueItem> tagMap = tagCompound == null ? null : NBTTool.nbtToItemCount((NBTTagList) tagCompound.getTag("itemcountmap"));
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
