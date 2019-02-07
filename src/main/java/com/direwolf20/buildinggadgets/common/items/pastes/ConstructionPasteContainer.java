package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.tools.NBTTool;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionPasteContainer extends GenericPasteContainer {

    public int maxAmount;

    public ConstructionPasteContainer(String suffix, int maxAmount) {
        this.maxAmount = maxAmount;

        String name = "constructionpastecontainer" + suffix;
        setRegistryName(name);
        setUnlocalizedName(BuildingGadgets.MODID + name);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelBakery.registerItemVariants(this,
                new ModelResourceLocation(getRegistryName(), "inventory"),
                new ModelResourceLocation(getRegistryName() + "-half", "inventory"),
                new ModelResourceLocation(getRegistryName() + "-full", "inventory"),
                new ModelResourceLocation(getRegistryName() + "-quarter", "inventory"),
                new ModelResourceLocation(getRegistryName() + "-3quarter", "inventory"));
    }

    @Override
    public void setPastes(ItemStack stack, int amount) {
        NBTTool.getOrNewTag(stack).setInteger("amount", amount);
    }

    @Override
    public int getPastes(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }

        return stack.getTagCompound().getInteger("amount");
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
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        list.add(getAmountDisplayLocalized() + ": " + getPasteAmount(stack));
    }

    @Override
    public int getMaxAmount() {
        return maxAmount;
    }

}
