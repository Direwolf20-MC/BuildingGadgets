package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUndo {

    public static void encode(PacketUndo msg, PacketBuffer buf) {}

    public static PacketUndo decode(PacketBuffer buf) {
        return new PacketUndo();
    }

    public static class Handler {
        public static void handle(PacketUndo msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null)
                return;

            ItemStack stack = AbstractGadget.getGadget(player);
            if (stack.getItem() instanceof GadgetBuilding)
                GadgetBuilding.undoBuild(player);
                //else if (stack.getItem() instanceof GadgetCopyPaste)
                //GadgetCopyPaste.undoBuild(player, stack);
            else if (stack.getItem() instanceof GadgetDestruction)
                GadgetDestruction.undo(player, stack);
        }
    }
}
