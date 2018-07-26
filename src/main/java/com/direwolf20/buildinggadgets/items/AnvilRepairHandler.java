package com.direwolf20.buildinggadgets.items;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AnvilRepairHandler {
    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        if ((event.getLeft().getItem() instanceof GenericGadget) && (event.getRight().getItem() == Items.DIAMOND)) {
            event.setCost(3);
            event.setMaterialCost(1);
            ItemStack newItem = event.getLeft().copy();
            newItem.setItemDamage(0);
            event.setOutput(newItem);
        }
    }
}
