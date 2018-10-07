package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
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

    public int getMaxAmount() {
        return 0;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        player.setActiveHand(hand);
        InventoryPlayer inv = player.inventory;
        if (!world.isRemote) {
            for (int i = 0; i < 36; ++i) {
                ItemStack itemStack = inv.getStackInSlot(i);
                if (itemStack.getItem() instanceof ConstructionPaste) {
                    itemStack = InventoryManipulation.addPasteToContainer(player, itemStack);
                }
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, heldItem);
    }
}
