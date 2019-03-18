package com.direwolf20.buildinggadgets.common.integration.mods;

import com.direwolf20.buildinggadgets.common.integration.IPasteRecipeRegistry;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.Phase;

import cofh.api.util.ThermalExpansionHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

@IntegratedMod("thermalexpansion")
public class ThermalExpansion implements IPasteRecipeRegistry {
    public static final FluidStack WATER = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);

    @Override
    public Phase getPhase() {
        return Phase.INIT;
    }

    @Override
    public void registerHydrationRecipe(RecipieType type, ItemStack input, ItemStack output) {
        ThermalExpansionHelper.addTransposerFill(400, input, output, WATER, false);
    }

    @Override
    public void registerDeconstructRecipe(RecipieType type, ItemStack input, ItemStack output) {
        ThermalExpansionHelper.addPulverizerRecipe(type == RecipieType.BLOCK_TO_CHUNKS ? 400 : 100, input, output);
    }
}