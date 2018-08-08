package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.items.ConstructionPasteContainer;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

public class PasteContainerMeshDefinition implements ItemMeshDefinition {
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        int pasteAmt = ConstructionPasteContainer.getPasteAmount(stack);
        if (pasteAmt < 50) {
            return new ModelResourceLocation("buildinggadgets:constructionpastecontainer", "inventory");
        } else if (pasteAmt >= 50 && pasteAmt < 100) {
            return new ModelResourceLocation("buildinggadgets:constructionpastecontainer-half", "inventory");
        } else {
            return new ModelResourceLocation("buildinggadgets:constructionpastecontainer-full", "inventory");
        }
    }
}
