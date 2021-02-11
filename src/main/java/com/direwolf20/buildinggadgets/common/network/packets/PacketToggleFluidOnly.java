package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.old_items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.old_items.GadgetDestruction;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleFluidOnly {
    public static void encode(PacketToggleFluidOnly msg, PacketBuffer buffer) {}
    public static PacketToggleFluidOnly decode(PacketBuffer buffer) { return new PacketToggleFluidOnly(); }

    public static class Handler {
        public static void handle(final PacketToggleFluidOnly msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
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
