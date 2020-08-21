package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.items.ConstructionPasteContainer;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
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
    public ItemStack getCraftingResult(CraftingInventory craftingInventory) {
        final ItemStack output = super.getCraftingResult(craftingInventory); // Get the default output
        if (!output.isEmpty()) {
            int totalPaste = 0;
            for (int i = 0; i < craftingInventory.getSizeInventory(); i++) { // For each slot in the crafting inventory,
                final ItemStack ingredient = craftingInventory.getStackInSlot(i); // Get the ingredient in the slot
                if (ingredient.getItem() instanceof ConstructionPasteContainer) // If it's a Paste Container,
                    totalPaste += ConstructionPasteContainer.getPasteAmount(ingredient);
            }
            ConstructionPasteContainer.setPasteAmount(output, totalPaste);
        }
        return output; // Return the modified output
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static final class Serializer extends ShapedRecipe.Serializer {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public ShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe recipe = super.read(recipeId, json);
            return new RecipeConstructionPaste(recipe.getId(), recipe.getGroup(), recipe.getRecipeWidth(),
                    recipe.getRecipeHeight(), recipe.getIngredients(), recipe.getRecipeOutput());
        }
    }
}
