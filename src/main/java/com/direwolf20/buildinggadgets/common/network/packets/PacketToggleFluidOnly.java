package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleFluidOnly {
    public static void encode(PacketToggleFluidOnly msg, FriendlyByteBuf buffer) {}
    public static PacketToggleFluidOnly decode(FriendlyByteBuf buffer) { return new PacketToggleFluidOnly(); }

    public static class Handler {
        public static void handle(final PacketToggleFluidOnly msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                if (stack.getItem() instanceof GadgetDestruction) {
                    GadgetDestruction.toggleFluidMode(stack);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}