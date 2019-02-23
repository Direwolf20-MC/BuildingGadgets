package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.utils.helpers.InventoryHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ItemPickupHandler {
    @SubscribeEvent
    public static void GetDrops(EntityItemPickupEvent event) {
        EntityItem entityItem = event.getItem();
        ItemStack itemStack = entityItem.getItem();
        if (itemStack.getItem() instanceof ConstructionPaste) {
            itemStack = InventoryHelper.addPasteToContainer(event.getEntityPlayer(), itemStack);
            entityItem.setItem(itemStack);
        }
    }
}
