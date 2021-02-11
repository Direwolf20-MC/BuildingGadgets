package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.old_items.AbstractGadget;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleRayTraceFluid {

    public static void encode(PacketToggleRayTraceFluid msg, PacketBuffer buffer) {}
    public static PacketToggleRayTraceFluid decode(PacketBuffer buffer) { return new PacketToggleRayTraceFluid(); }

    public static class Handler {
        public static void handle(final PacketToggleRayTraceFluid msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
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
