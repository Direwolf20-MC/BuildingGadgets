package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventBusSubscriber
public class AnvilRepairHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (Config.GADGETS.poweredByFE.get() && (event.getLeft().getItem() instanceof GadgetGeneric) && (event.getRight().getItem() == Items.DIAMOND)) {
            event.setCost(3);
            event.setMaterialCost(1);
            ItemStack newItem = event.getLeft().copy();
            newItem.setDamage(0);
            event.setOutput(newItem);
        }
    }
}
