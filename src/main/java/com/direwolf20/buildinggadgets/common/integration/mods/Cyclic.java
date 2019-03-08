package com.direwolf20.buildinggadgets.common.integration.mods;

import com.direwolf20.buildinggadgets.common.integration.IPasteRecipeRegistry;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.lothrazar.cyclicmagic.block.hydrator.RecipeHydrate;

import net.minecraft.item.ItemStack;

@IntegratedMod("cyclicmagic")
public class Cyclic implements IPasteRecipeRegistry {

    @Override
    public void registerHydrationRecipe(RecipieType type, ItemStack input, ItemStack output) {
        RecipeHydrate.addRecipe(new RecipeHydrate(input, output));
    }
}