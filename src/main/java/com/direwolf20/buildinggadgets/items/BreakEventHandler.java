package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;
import java.util.List;

public class BreakEventHandler {
    @SubscribeEvent
    public void GetDrops(BlockEvent.HarvestDropsEvent event) {
        EntityPlayer player = event.getHarvester();
        if (player == null) {return;}
        ItemStack heldItem = player.getHeldItemMainhand();
        List<ItemStack> drops = event.getDrops();
        if (heldItem.getItem() instanceof ExchangerTool) {
            drops.removeIf(item->InventoryManipulation.giveItem(item,player));
        }

    }
}

