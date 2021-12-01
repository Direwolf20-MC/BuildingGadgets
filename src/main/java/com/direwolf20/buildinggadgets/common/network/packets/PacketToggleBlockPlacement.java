package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleBlockPlacement {

    public static void encode(PacketToggleBlockPlacement msg, FriendlyByteBuf buffer) {}
    public static PacketToggleBlockPlacement decode(FriendlyByteBuf buffer) { return new PacketToggleBlockPlacement(); }

    public static class Handler {
        public static void handle(final PacketToggleBlockPlacement msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
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