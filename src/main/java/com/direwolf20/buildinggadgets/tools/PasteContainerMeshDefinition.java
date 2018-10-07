package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.items.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.items.ConstructionPasteContainerT2;
import com.direwolf20.buildinggadgets.items.ConstructionPasteContainerT3;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

public class PasteContainerMeshDefinition implements ItemMeshDefinition {
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        int pasteAmt = ConstructionPasteContainer.getPasteAmount(stack);
        int maxAmount = 0;
        if (stack.getItem() instanceof ConstructionPasteContainer)
            maxAmount = ConstructionPasteContainer.maxAmount;
        else if (stack.getItem() instanceof ConstructionPasteContainerT2)
            maxAmount = ConstructionPasteContainerT2.maxAmount;
        else if (stack.getItem() instanceof ConstructionPasteContainerT3)
            maxAmount = ConstructionPasteContainerT3.maxAmount;
        if (pasteAmt < maxAmount / 4) {
            return new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory");
        } else if (pasteAmt >= maxAmount / 4 && pasteAmt < maxAmount / 2) {
            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-quarter", "inventory");
        } else if (pasteAmt >= maxAmount / 2 && pasteAmt < maxAmount * 3 / 4) {
            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-half", "inventory");
        } else if (pasteAmt >= maxAmount * 3 / 4 && pasteAmt < maxAmount) {
            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-3quarter", "inventory");
        } else {
            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-full", "inventory");
        }
    }
}
