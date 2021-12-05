package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleRayTraceFluid {

    public static void encode(PacketToggleRayTraceFluid msg, FriendlyByteBuf buffer) {}
    public static PacketToggleRayTraceFluid decode(FriendlyByteBuf buffer) { return new PacketToggleRayTraceFluid(); }

    public static class Handler {
        public static void handle(final PacketToggleRayTraceFluid msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                if (!stack.isEmpty())
                    AbstractGadget.toggleRayTraceFluid(player, stack);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}