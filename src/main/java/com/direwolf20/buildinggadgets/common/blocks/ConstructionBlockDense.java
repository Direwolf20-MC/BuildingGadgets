package com.direwolf20.buildinggadgets.common.blocks;

import java.util.Random;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.ModItems;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

public class ConstructionBlockDense extends BlockModBase {

    public ConstructionBlockDense() {
        super(Material.ROCK, 3F, "constructionblock_dense");
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.constructionPaste;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return SyncedConfig.pasteDroppedMin + random.nextInt(SyncedConfig.pasteDroppedMax - SyncedConfig.pasteDroppedMin + 1);
    }
}