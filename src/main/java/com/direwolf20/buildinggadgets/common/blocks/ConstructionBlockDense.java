package com.direwolf20.buildinggadgets.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public class ConstructionBlockDense extends Block {
    public ConstructionBlockDense() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3f, 0f).harvestTool(ToolType.PICKAXE));
    }
}
