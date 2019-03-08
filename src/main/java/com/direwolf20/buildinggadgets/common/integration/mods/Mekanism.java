package com.direwolf20.buildinggadgets.common.integration.mods;

import com.direwolf20.buildinggadgets.common.integration.IPasteRecipeRegistry;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;

import mekanism.api.MekanismAPI;
import net.minecraft.item.ItemStack;

@IntegratedMod("mekanism")
public class Mekanism implements IPasteRecipeRegistry {

    @Override
    public Phase getPhase() {
        return Phase.INIT;
    }

    @Override
    public void registerDeconstructRecipe(RecipieType type, ItemStack input, ItemStack output) {
        MekanismAPI.recipeHelper().addCrusherRecipe(input, output);
    }
}