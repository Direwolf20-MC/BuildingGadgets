package com.direwolf20.buildinggadgets.common.integration.mods;

import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.integration.IPasteRecipeRegistry;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import net.minecraft.item.ItemStack;

@IntegratedMod(ActuallyAdditionsAPI.MOD_ID)
public class ActuallyAdditions implements IPasteRecipeRegistry {

    @Override
    public void registerDeconstructRecipe(RecipieType stage, ItemStack input, ItemStack output) {
        ActuallyAdditionsAPI.addCrusherRecipe(input, output, ItemStack.EMPTY, 0);
    }
}