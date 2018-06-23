package com.direwolf20.buildinggadgets.Blocks;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class EffectBlock extends Block {

    public EffectBlock() {
        super(Material.ROCK);
        setHardness(20.0f);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);   // the block will appear on the Blocks tab in creative
        setUnlocalizedName(BuildingGadgets.MODID + ".effectblock");     // Used for localization (en_US.lang)
        setRegistryName("effectblock");        // The unique name (within your mod) that identifies this block
    }
}
