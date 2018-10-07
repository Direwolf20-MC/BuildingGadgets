package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConstructionPasteContainerT3 extends GenericPasteContainer {

    public static int maxAmount = 8192;

    public ConstructionPasteContainerT3() {
        setRegistryName("constructionpastecontainert3");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".constructionpastecontainert3");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
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
    public int getMaxAmount() {
        return maxAmount;
    }
}
