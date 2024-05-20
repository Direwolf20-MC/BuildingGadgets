package com.direwolf20.buildinggadgets.common.config;

import com.direwolf20.buildinggadgets.common.items.ConstructionPasteContainer;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

public class RecipeConstructionPaste extends ShapedRecipe {

    public RecipeConstructionPaste(ResourceLocation id, String group, CraftingBookCategory category, int recipeWidth,
                                   int recipeHeight, NonNullList<Ingredient> recipeItems, ItemStack recipeOutput) {
        super(id, group, category, recipeWidth, recipeHeight, recipeItems, recipeOutput);
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingInventory, RegistryAccess registryAccess) {
        final ItemStack output = super.assemble(craftingInventory, registryAccess); // Get the default output
        if (!output.isEmpty()) {
            int totalPaste = 0;
            for (int i = 0; i < craftingInventory.getContainerSize(); i++) { // For each slot in the crafting inventory,
                final ItemStack ingredient = craftingInventory.getItem(i); // Get the ingredient in the slot
                if (ingredient.getItem() instanceof ConstructionPasteContainer) // If it's a Paste Container,
                    totalPaste += ConstructionPasteContainer.getPasteAmount(ingredient);
            }
            ConstructionPasteContainer.setPasteAmount(output, totalPaste);
        }
        return output; // Return the modified output
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static final class Serializer extends ShapedRecipe.Serializer {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapedRecipe recipe = super.fromJson(recipeId, json);
            return new RecipeConstructionPaste(recipe.getId(), recipe.getGroup(), CraftingBookCategory.MISC, recipe.getRecipeWidth(),
                    recipe.getRecipeHeight(), recipe.getIngredients(), recipe.getResultItem(RegistryAccess.EMPTY));
        }
    }
}
