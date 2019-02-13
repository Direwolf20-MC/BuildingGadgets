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
            EntityPlayerMP player = ctx.get().getSender();
            if (player == null)
                return;

            ItemStack stack = GadgetGeneric.getGadget(player);
            if (stack.getItem() instanceof GadgetBuilding)
                GadgetBuilding.undoBuild(player);
            else if (stack.getItem() instanceof GadgetCopyPaste)
                GadgetCopyPaste.undoBuild(player, stack);
            else if (stack.getItem() instanceof GadgetDestruction)
                GadgetDestruction.undo(player, stack);
        }
    }
}
