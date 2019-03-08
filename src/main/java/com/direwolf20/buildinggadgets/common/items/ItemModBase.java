package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemModBase extends Item {

    public ItemModBase() {}

    public ItemModBase(String name) {
        setRegistryName(name);
        setUnlocalizedName(String.join(".", BuildingGadgets.MODID, name));
        setCreativeTab(BuildingGadgets.BUILDING_CREATIVE_TAB);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}