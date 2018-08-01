package com.direwolf20.buildinggadgets.blocks.Models;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ConstructionID {
    private final String registryName;
    private final int meta;
    private IBlockState blockState;

    public ConstructionID(IBlockState mimicBlock) {
        Block block = mimicBlock.getBlock();
        this.registryName = block.getRegistryName().toString();
        this.meta = block.getMetaFromState(mimicBlock);
        this.blockState = mimicBlock;
    }

    public IBlockState getBlockState() {
        //return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName)).getStateFromMeta(meta);
        return blockState;
    }

    @Override
    public String toString() {
        return registryName + '@' + meta;
    }
}
