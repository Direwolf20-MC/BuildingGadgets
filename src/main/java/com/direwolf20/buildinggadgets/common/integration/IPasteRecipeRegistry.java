package com.direwolf20.buildinggadgets.common.integration;

import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IIntegratedMod;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.Phase;
import com.direwolf20.buildinggadgets.common.items.ModItems;

import net.minecraft.item.ItemStack;

public interface IPasteRecipeRegistry extends IIntegratedMod {

    default Phase getPhase() {
        return Phase.POST_INIT;
    }

    @Override
    default void initialize(Phase phase) {
        if (getPhase() == phase && Config.enablePaste)
            registerAllRecipes();
    }

    default void registerAllRecipes() {
        registerHydrationRecipe(RecipieType.POWDER_TO_BLOCK, new ItemStack(ModBlocks.constructionBlockPowder), new ItemStack(ModBlocks.constructionBlockDense));
        registerDeconstructRecipe(RecipieType.BLOCK_TO_CHUNKS, new ItemStack(ModBlocks.constructionBlockDense), new ItemStack(ModItems.constructionChunkDense, 4));
        registerDeconstructRecipe(RecipieType.CHUNK_TO_PASTE, new ItemStack(ModItems.constructionChunkDense), new ItemStack(ModItems.constructionPaste));
    }

    default void registerHydrationRecipe(RecipieType type, ItemStack input, ItemStack output) {}

    default void registerDeconstructRecipe(RecipieType type, ItemStack input, ItemStack output) {}

    public static enum RecipieType {
        POWDER_TO_BLOCK, BLOCK_TO_CHUNKS, CHUNK_TO_PASTE;
    }
}