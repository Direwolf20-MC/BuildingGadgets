package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleBlockPlacement {

    public static void encode(PacketToggleBlockPlacement msg, PacketBuffer buffer) {}
    public static PacketToggleBlockPlacement decode(PacketBuffer buffer) { return new PacketToggleBlockPlacement(); }

    public static class Handler {
        public static void handle(final PacketToggleBlockPlacement msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                if (stack.getItem() instanceof GadgetBuilding)
                    GadgetBuilding.togglePlaceAtop(player, stack);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}