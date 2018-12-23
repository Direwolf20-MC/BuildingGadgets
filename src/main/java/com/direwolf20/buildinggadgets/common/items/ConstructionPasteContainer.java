package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConstructionPasteContainer extends GenericPasteContainer {

    public static int maxAmount = 512;

    public ConstructionPasteContainer() {
        setRegistryName("constructionpastecontainer");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".constructionpastecontainer");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
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
