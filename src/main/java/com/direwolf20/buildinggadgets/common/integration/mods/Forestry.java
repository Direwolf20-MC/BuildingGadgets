package com.direwolf20.buildinggadgets.common.integration.mods;

import com.direwolf20.buildinggadgets.common.integration.IPasteRecipeRegistry;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;

import forestry.api.recipes.RecipeManagers;
import net.minecraft.item.ItemStack;

@IntegratedMod("forestry")
public class Forestry implements IPasteRecipeRegistry {

    @Override
    public void registerHydrationRecipe(RecipieType type, ItemStack input, ItemStack output) {
        RecipeManagers.moistenerManager.addRecipe(input, output, 80);
    }
}