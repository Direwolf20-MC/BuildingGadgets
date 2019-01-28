package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionPasteContainer extends Item {

    private int maxAmount;

    public ConstructionPasteContainer(Builder builder, int maxAmount) {
        super(builder);

        this.maxAmount = maxAmount;
    }

    @OnlyIn(Dist.CLIENT)
    public void initModel() {
// @todo: reimplement @since 1.13.x
//        ModelBakery.registerItemVariants(this,
//                new ModelResourceLocation(getRegistryName(), "inventory"),
//                new ModelResourceLocation(getRegistryName() + "-half", "inventory"),
//                new ModelResourceLocation(getRegistryName() + "-full", "inventory"),
//                new ModelResourceLocation(getRegistryName() + "-quarter", "inventory"),
//                new ModelResourceLocation(getRegistryName() + "-3quarter", "inventory"));
    }

    public static void setPasteAmount(ItemStack stack, int amount) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInt("amount", amount);
        stack.setTag(tagCompound);
    }

    public static int getPasteAmount(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        int amount = 0;
        if (tagCompound == null) {
            setPasteAmount(stack, 0);
            return amount;
        }
        amount = tagCompound.getInt("amount");
        return amount;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        tooltip.add(new TextComponentString(TextFormatting.WHITE + I18n.format("tooltip.pasteContainer.amount") + ": " + getPasteAmount(stack)));
    }

    public int getMaxAmount() {
        return maxAmount;
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
