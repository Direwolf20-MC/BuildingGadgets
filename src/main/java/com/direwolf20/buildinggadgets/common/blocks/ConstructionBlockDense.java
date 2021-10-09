package com.direwolf20.buildinggadgets.common.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

public class ConstructionBlockDense extends Block {
    public ConstructionBlockDense() {
        super(Block.Properties.of(Material.STONE).strength(3f, 0f));
    }
}
