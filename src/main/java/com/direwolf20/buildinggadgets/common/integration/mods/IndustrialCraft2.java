package com.direwolf20.buildinggadgets.common.integration.mods;

import java.util.Collections;

import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.integration.IPasteRecipeRegistry;

import ic2.api.recipe.Recipes;
import net.minecraft.item.ItemStack;

@IntegratedMod("ic2")
public class IndustrialCraft2 implements IPasteRecipeRegistry {

    @Override
    public void registerDeconstructRecipe(RecipieType type, ItemStack input, ItemStack output) {
        Recipes.macerator.addRecipe(Recipes.inputFactory.forStack(input), Collections.singletonList(output), null, false);
    }
}