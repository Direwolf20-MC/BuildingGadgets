package com.direwolf20.buildinggadgets.common.tools;

//import com.direwolf20.buildinggadgets.common.items.ConstructionPasteContainer;
//import com.direwolf20.buildinggadgets.common.items.GenericPasteContainer;
//import net.minecraft.client.renderer.ItemMeshDefinition;
//import net.minecraft.client.renderer.block.model.ModelResourceLocation;
//import net.minecraft.item.ItemStack;
//
//public class PasteContainerMeshDefinition implements ItemMeshDefinition {
//    @Override
//    public ModelResourceLocation getModelLocation(ItemStack stack) {
//        int pasteAmt = ConstructionPasteContainer.getPasteAmount(stack);
//        int maxAmount = ((GenericPasteContainer) stack.getItem()).getMaxAmount();
//        if (pasteAmt < maxAmount / 4) {
//            return new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory");
//        } else if (pasteAmt >= maxAmount / 4 && pasteAmt < maxAmount / 2) {
//            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-quarter", "inventory");
//        } else if (pasteAmt >= maxAmount / 2 && pasteAmt < maxAmount * 3 / 4) {
//            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-half", "inventory");
//        } else if (pasteAmt >= maxAmount * 3 / 4 && pasteAmt < maxAmount) {
//            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-3quarter", "inventory");
//        } else {
//            return new ModelResourceLocation(stack.getItem().getRegistryName() + "-full", "inventory");
//        }
//    }
//}
