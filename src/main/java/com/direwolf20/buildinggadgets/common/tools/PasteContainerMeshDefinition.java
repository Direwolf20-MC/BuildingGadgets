package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.pastes.GenericPasteContainer;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

public class PasteContainerMeshDefinition implements ItemMeshDefinition {
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        int pasteAmt = ConstructionPasteContainer.getPasteAmount(stack);
        int maxCapacity = ((GenericPasteContainer) stack.getItem()).getMaxCapacity();
        if (pasteAmt < maxCapacity / 4) {
            return new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory");
        } else if (pasteAmt >= maxCapacity / 4 && pasteAmt < maxCapacity / 2) {
            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-quarter", "inventory");
        } else if (pasteAmt >= maxCapacity / 2 && pasteAmt < maxCapacity * 3 / 4) {
            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-half", "inventory");
        } else if (pasteAmt >= maxCapacity * 3 / 4 && pasteAmt < maxCapacity) {
            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-3quarter", "inventory");
        } else {
            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-full", "inventory");
        }
    }
}
