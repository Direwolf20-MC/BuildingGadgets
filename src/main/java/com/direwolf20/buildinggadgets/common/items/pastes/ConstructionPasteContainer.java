package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInteger("amount", amount);
        stack.setTagCompound(tagCompound);

        stack.setTagCompound(tagCompound);
    }

    @Override
    public int getPastes(ItemStack stack) {
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
    public int getMaxAmount() {
        return maxAmount;
    }

}
