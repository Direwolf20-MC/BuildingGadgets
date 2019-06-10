package com.direwolf20.buildinggadgets.common.blocks;

import net.minecraft.block.Block;

public class ConstructionBlockDense extends Block {

    public ConstructionBlockDense(Properties properties) {
        super(properties);
    }

// fixme: we need loot tables now.
//    @Override
//    public IItemProvider getItemDropped(BlockState state, World world, BlockPos pos, int fortune) {
//        return BGItems.constructionPaste;
//    }
//
//    @Override
//    public int getItemsToDropCount(BlockState state, int fortune, World world, BlockPos pos, Random random) {
//        int min = Config.GENERAL.pasteDroppedMin.get();
//        return min + random.nextInt(Config.GENERAL.pasteDroppedMax.get() - min + 1);
//    }
}