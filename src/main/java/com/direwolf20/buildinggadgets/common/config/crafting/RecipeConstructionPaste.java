package com.direwolf20.buildinggadgets.common.config.crafting;

import com.direwolf20.buildinggadgets.common.items.pastes.GenericPasteContainer;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class RecipeConstructionPaste extends ShapedRecipe {

    public RecipeConstructionPaste(ResourceLocation id, String group, int recipeWidth,
            int recipeHeight, NonNullList<Ingredient> recipeItems, ItemStack recipeOutput) {
        super(id, group, recipeWidth, recipeHeight, recipeItems, recipeOutput);
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        final ItemStack output = super.getCraftingResult(inv); // Get the default output
        if (!output.isEmpty()) {
            int totalPaste = 0;
            for (int i = 0; i < inv.getSizeInventory(); i++) { // For each slot in the crafting inventory,
                final ItemStack ingredient = inv.getStackInSlot(i); // Get the ingredient in the slot
                if (ingredient.getItem() instanceof GenericPasteContainer) // If it's a Paste Container,
                    totalPaste += GenericPasteContainer.getPasteAmount(ingredient);
            }
            GenericPasteContainer.setPasteAmount(output, totalPaste);
        }
        return output; // Return the modified output
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        public static final ResourceLocation NAME = new ResourceLocation(Reference.MODID, "construction_paste");

        @Override
        public ResourceLocation getName() {
            return NAME;
        }

        @Override
        public ShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe recipe = super.read(recipeId, json);
            return new RecipeConstructionPaste(recipe.getId(), recipe.getGroup(), recipe.getRecipeWidth(),
                    recipe.getRecipeHeight(), recipe.getIngredients(), recipe.getRecipeOutput());
        }
    }
}
