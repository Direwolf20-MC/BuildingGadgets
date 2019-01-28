package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUndoKey {

    public static void encode(PacketUndoKey msg, PacketBuffer buf) {}
    public static PacketUndoKey decode(PacketBuffer buf) { return null; }

    public static class Handler {
        public static void handle(PacketUndoKey msg, Supplier<NetworkEvent.Context> ctx) {
            EntityPlayerMP playerEntity = ctx.get().getSender();
            if( playerEntity == null ) return;

            ItemStack heldItem = GadgetGeneric.getGadget(playerEntity);
            if (heldItem.isEmpty())
                return;

            if (heldItem.getItem() instanceof GadgetBuilding) {
                GadgetBuilding gadgetBuilding = (GadgetBuilding) (heldItem.getItem());
                gadgetBuilding.undoBuild(playerEntity);
            } else if (heldItem.getItem() instanceof GadgetExchanger) {
                GadgetExchanger gadgetExchanger = (GadgetExchanger) (heldItem.getItem());
                gadgetExchanger.toggleFuzzy(playerEntity, heldItem);
            } else if (heldItem.getItem() instanceof GadgetCopyPaste) {
                GadgetCopyPaste gadgetCopyPaste = (GadgetCopyPaste) (heldItem.getItem());
                gadgetCopyPaste.undoBuild(playerEntity, heldItem);
            } else if (heldItem.getItem() instanceof GadgetDestruction) {
                GadgetDestruction gadgetDestruction = (GadgetDestruction) (heldItem.getItem());
                gadgetDestruction.undo(playerEntity, heldItem);
            }
        }
    }
}
