package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
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
            if (! stack.isEmpty() && !(stack.getItem() instanceof GadgetExchanger))
                ((AbstractGadget) stack.getItem()).undo(player.level, player, stack);

            ctx.get().setPacketHandled(true);
        }
    }
}
