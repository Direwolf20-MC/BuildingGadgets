package com.direwolf20.buildinggadgets.eventhandlers;

import com.direwolf20.buildinggadgets.items.ConstructionPaste;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemPickupHandler {
    @SubscribeEvent
    public void GetDrops(EntityItemPickupEvent event) {
        EntityItem entityItem = event.getItem();
        ItemStack itemStack = entityItem.getItem();
        if (itemStack.getItem() instanceof ConstructionPaste) {
            itemStack = InventoryManipulation.addPasteToContainer(event.getEntityPlayer(), itemStack);
            entityItem.setItem(itemStack);
        }
    }
}
