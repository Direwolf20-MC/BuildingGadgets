package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConstructionPasteContainerT2 extends GenericPasteContainer {

    public static int maxAmount = 2048;

    public ConstructionPasteContainerT2() {
        setRegistryName("constructionpastecontainert2");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".constructionpastecontainert2");     // Used for localization (en_US.lang)
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
