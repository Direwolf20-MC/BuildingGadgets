package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.utils.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import com.google.common.collect.Multiset;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITemplate {

    static ItemStack getTemplate(EntityPlayer player) {
        ItemStack mainhand = player.getHeldItemMainhand();
        if (mainhand.getItem() instanceof ITemplate)
            return mainhand;

        ItemStack offhand = player.getHeldItemOffhand();
        if (offhand.getItem() instanceof ITemplate)
            return offhand;

        return ItemStack.EMPTY;
    }

    @Nullable
    String getUUID(ItemStack stack);

    WorldSave getWorldSave(World world);

    default void setItemCountMap(ItemStack stack, Multiset<UniqueItem> tagMap) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList tagList = GadgetUtils.itemCountToNBT(tagMap);
        tagCompound.setTag("itemcountmap", tagList);
        stack.setTag(tagCompound);
    }

    @Nonnull
    default Multiset<UniqueItem> getItemCountMap(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        Multiset<UniqueItem> tagMap = tagCompound == null ? null : GadgetUtils.nbtToItemCount((NBTTagList) tagCompound.getTag("itemcountmap"));
        if (tagMap == null)
            throw new IllegalArgumentException("ITemplate#getItemCountMap faild to retieve tag map from " + GadgetUtils.getStackErrorSuffix(stack));

        return tagMap;
    }

    default int getCopyCounter(ItemStack stack) {
        return GadgetUtils.getStackTag(stack).getInt(NBTKeys.TEMPLATE_COPY_COUNT);
    }

    default void setCopyCounter(ItemStack stack, int counter) {
        NBTTagCompound tagCompound = GadgetUtils.getStackTag(stack);
        tagCompound.setInt(NBTKeys.TEMPLATE_COPY_COUNT, counter);
        stack.setTag(tagCompound);
    }

    default void incrementCopyCounter(ItemStack stack) {
        setCopyCounter(stack, getCopyCounter(stack) + 1);
    }

    default void setStartPos(ItemStack stack, BlockPos startPos) {
        GadgetUtils.writePOSToNBT(stack, startPos, NBTKeys.GADGET_START_POS);
    }

    @Nullable
    default BlockPos getStartPos(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_START_POS);
    }

    default void setEndPos(ItemStack stack, BlockPos startPos) {
        GadgetUtils.writePOSToNBT(stack, startPos, NBTKeys.GADGET_END_POS);
    }

    @Nullable
    default BlockPos getEndPos(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_END_POS);
    }

    @OnlyIn(Dist.CLIENT)
    default void openGui() {
        GuiMod.MATERIAL_LIST.openScreen(Minecraft.getInstance().player);
    }

}
