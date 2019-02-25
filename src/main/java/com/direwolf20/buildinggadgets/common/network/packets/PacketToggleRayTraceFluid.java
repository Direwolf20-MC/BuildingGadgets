package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.entity.player.EntityPlayerMP;
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
                EntityPlayerMP player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = GadgetGeneric.getGadget(player);
                if (!stack.isEmpty())
                    GadgetGeneric.toggleRayTraceFluid(player, stack);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}