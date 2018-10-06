package com.direwolf20.buildinggadgets.items;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class GenericPasteContainer extends Item {
    public static void setPasteAmount(ItemStack stack, int amount) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInteger("amount", amount);
        stack.setTagCompound(tagCompound);
    }

    public static int getPasteAmount(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        int amount = 0;
        if (tagCompound == null) {
            setPasteAmount(stack, 0);
            return amount;
        }
        amount = tagCompound.getInteger("amount");
        return amount;
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, player, list, b);
        list.add(TextFormatting.WHITE + I18n.format("tooltip.pasteContainer.amount") + ": " + getPasteAmount(stack));
    }
}
