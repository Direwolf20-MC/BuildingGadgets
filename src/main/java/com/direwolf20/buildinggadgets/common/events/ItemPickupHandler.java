package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.items.ConstructionPaste;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ItemPickupHandler {

    @SubscribeEvent
    public static void GetDrops(EntityItemPickupEvent event) {
        ItemEntity entityItem = event.getItem();
        ItemStack itemStack = entityItem.getItem();

        if (itemStack.getItem() instanceof ConstructionPaste) {
            itemStack = InventoryHelper.addPasteToContainer(event.getPlayer(), itemStack);
            entityItem.setItem(itemStack);
        }
    }
}
