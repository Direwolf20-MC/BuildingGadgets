package com.direwolf20.buildinggadgets.common.integration.mods;

import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.integration.IPasteRecipeRegistry;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import net.minecraft.item.ItemStack;

@IntegratedMod(Lib.MODID)
public class ImmersiveEngineering implements IPasteRecipeRegistry {

    @Override
    public void registerDeconstructRecipe(RecipieType type, ItemStack input, ItemStack output) {
        CrusherRecipe.addRecipe(output, input, type == RecipieType.BLOCK_TO_CHUNKS ? 4000 : 1000);
    }
}