package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.utils.NBTUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

public class ConstructionPasteContainer extends GenericPasteContainer {

    private IntSupplier maxCapacity;

    public ConstructionPasteContainer(Builder builder, RegularPasteContainerTypes type) {
        super(builder, type.itemSuffix);
        maxCapacity = type.capacitySupplier;
    }

    @OnlyIn(Dist.CLIENT)
    public void initModel() {
//        ModelBakery.registerItemVariants(this,
//                new ModelResourceLocation(getRegistryName(), "inventory"),
//                new ModelResourceLocation(getRegistryName() + "-half", "inventory"),
//                new ModelResourceLocation(getRegistryName() + "-full", "inventory"),
//                new ModelResourceLocation(getRegistryName() + "-quarter", "inventory"),
//                new ModelResourceLocation(getRegistryName() + "-3quarter", "inventory"));
    }

    @Override
    public void setPasteCount(ItemStack stack, int amount) {
        NBTUtil.getOrNewTag(stack).setInt("amount", amount);
    }

    @Override
    public int getPasteCount(ItemStack stack) {
        return !stack.hasTag() ? 0 : stack.getTag().getInt("amount");
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
                    InventoryManipulation.addPasteToContainer(player, itemStack);
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        list.add(new TextComponentTranslation(getAmountDisplayLocalized() + ": " + getPasteAmount(stack)).setStyle(new Style().setColor(TextFormatting.WHITE)));
    }

    @Override
    public int getMaxCapacity() {
        return maxCapacity.getAsInt();
    }

}
