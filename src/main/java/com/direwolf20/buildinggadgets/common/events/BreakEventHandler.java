package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.List;

@EventBusSubscriber
public class BreakEventHandler {
    @SubscribeEvent
    public static void GetDrops(BlockEvent.HarvestDropsEvent event) {
        //If you are holding an exchanger gadget and break a block, put it into your inventory
        //This allows us to use the BreakBlock event on our exchanger, to properly remove blocks from the world.
        PlayerEntity player = event.getHarvester();
        if (player == null)
            return;

        ItemStack heldItem = GadgetGeneric.getGadget(player);
        if (heldItem.isEmpty())
            return;

        List<ItemStack> drops = event.getDrops();
        drops.removeIf(item -> InventoryHelper.giveItem(item, player, event.getWorld()));
    }
}

