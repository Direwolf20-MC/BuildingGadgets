package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleConnectedArea {

    public static void encode(PacketToggleConnectedArea msg, FriendlyByteBuf buffer) {}
    public static PacketToggleConnectedArea decode(FriendlyByteBuf buffer) { return new PacketToggleConnectedArea(); }

    public static class Handler {
        public static void handle(final PacketToggleConnectedArea msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                if (stack.getItem() instanceof GadgetExchanger || stack.getItem() instanceof GadgetBuilding || stack.getItem() instanceof GadgetDestruction)
                    AbstractGadget.toggleConnectedArea(player, stack);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}