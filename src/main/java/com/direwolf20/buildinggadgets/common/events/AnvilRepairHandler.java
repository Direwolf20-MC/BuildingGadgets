package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AnvilRepairHandler {
    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event) {
        if (SyncedConfig.poweredByFE && (event.getLeft().getItem() instanceof GadgetGeneric) && (event.getRight().getItem() == Items.DIAMOND)) {
            event.setCost(3);
            event.setMaterialCost(1);
            ItemStack newItem = event.getLeft().copy();
            newItem.setItemDamage(0);
            event.setOutput(newItem);
        }
    }
}
