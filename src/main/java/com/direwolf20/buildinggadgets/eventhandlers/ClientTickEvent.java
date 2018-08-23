package com.direwolf20.buildinggadgets.eventhandlers;

import com.direwolf20.buildinggadgets.ModItems;
import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.network.PacketRequestBlockMap;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.tools.PasteToolBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class ClientTickEvent {

    private static int counter = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        counter++;
        if (counter > 500) {
            //System.out.println("Timer Fired");
            counter = 0;
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (player == null) return;

            ArrayList<Integer> slots = InventoryManipulation.findItem(ModItems.copyPasteTool, 0, player.inventory);
            if (slots.size() == 0) return;

            for (int slot : slots) {
                ItemStack stack = player.inventory.getStackInSlot(slot);
                String UUID = CopyPasteTool.getUUID(stack);
                if (UUID != null) {
                    if (PasteToolBufferBuilder.isUpdateNeeded(UUID, stack)) {
                        System.out.println("BlockMap Update Needed for UUID: " + UUID + " in slot " + slot);
                        PacketHandler.INSTANCE.sendToServer(new PacketRequestBlockMap(CopyPasteTool.getUUID(stack)));
                    }
                }
            }
        }
    }
}
