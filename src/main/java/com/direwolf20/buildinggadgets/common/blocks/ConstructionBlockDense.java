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
        return BGItems.constructionPaste;
    }

    @Override
    public int getItemsToDropCount(IBlockState state, int fortune, World world, BlockPos pos, Random random) {
        int min = Config.GENERAL.pasteDroppedMin.get();
        return min + random.nextInt(Config.GENERAL.pasteDroppedMax.get() - min + 1);
    }
}