package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class BreakEventHandler {
    @SubscribeEvent
    public void GetDrops(BlockEvent.HarvestDropsEvent event) {
        //If you are holding an exchanger gadget and break a block, put it into your inventory
        //This allows us to use the BreakBlock event on our exchanger, to properly remove blocks from the world.
        EntityPlayer player = event.getHarvester();
        if (player == null) {
            return;
        }
        ItemStack heldItem = player.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof ExchangerTool) && !(heldItem.getItem() instanceof BuildingTool)) {
            heldItem = player.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof ExchangerTool) && !(heldItem.getItem() instanceof BuildingTool)) {
                return;
            }
        }
        List<ItemStack> drops = event.getDrops();
        if ((heldItem.getItem() instanceof ExchangerTool) || (heldItem.getItem() instanceof BuildingTool)) {
            drops.removeIf(item -> InventoryManipulation.giveItem(item, player));
        }

    }
}

