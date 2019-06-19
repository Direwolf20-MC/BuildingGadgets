package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConstructionBlockDense extends Block {

    public ConstructionBlockDense(Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        int min = Config.GENERAL.pasteDroppedMin.get();
        Random random = new Random();
        return new ArrayList<ItemStack>() {{ //TODO change to LootTable and ensure Dire didn't break me
            add(new ItemStack(BGItems.constructionPaste, min + random.nextInt(Config.GENERAL.pasteDroppedMax.get() - min + 1)));
        }};
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