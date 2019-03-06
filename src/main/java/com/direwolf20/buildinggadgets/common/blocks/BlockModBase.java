package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockModBase extends Block {

    public BlockModBase(Material material, float hardness, String name) {
        super(material);
        setHardness(hardness);
        setCreativeTab(BuildingGadgets.BUILDING_CREATIVE_TAB);
        setUnlocalizedName(String.join(".", BuildingGadgets.MODID, name));
        setRegistryName(name);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}