package com.direwolf20.buildinggadgets.common.blocks;

import java.util.Random;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ConstructionBlockDense extends Block {

    public ConstructionBlockDense(Properties properties) {
        super(properties);
    }

    @Override
    public IItemProvider getItemDropped(IBlockState state, World world, BlockPos pos, int fortune) {
        return world.rand.nextDouble() < Config.GENERAL.denseConstructionChunkFraction.get() ? BGItems.constructionChunkDense : BGItems.constructionPaste;
    }

    @Override
    public int getItemsToDropCount(IBlockState state, int fortune, World world, BlockPos pos, Random random) {
        return Config.GENERAL.denseConstructionDropCount.get();
    }
}