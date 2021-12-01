package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUndo {

    public static void encode(PacketUndo msg, FriendlyByteBuf buf) {}

    public static PacketUndo decode(FriendlyByteBuf buf) {
        return new PacketUndo();
    }

    public static class Handler {
        public static void handle(PacketUndo msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player == null)
                return;

            ItemStack stack = AbstractGadget.getGadget(player);
            if (! stack.isEmpty() && !(stack.getItem() instanceof GadgetExchanger))
                ((AbstractGadget) stack.getItem()).undo(player.level, player, stack);

            ctx.get().setPacketHandled(true);
        }
    }
}
