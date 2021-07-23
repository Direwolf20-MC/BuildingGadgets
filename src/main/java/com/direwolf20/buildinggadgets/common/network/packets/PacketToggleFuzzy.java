package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleFuzzy {

    public static void encode(PacketToggleFuzzy msg, FriendlyByteBuf buffer) {}
    public static PacketToggleFuzzy decode(FriendlyByteBuf buffer) { return new PacketToggleFuzzy(); }

    public static class Handler {
        public static void handle(final PacketToggleFuzzy msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                if (stack.getItem() instanceof GadgetExchanger || stack.getItem() instanceof GadgetBuilding
                        || (stack.getItem() instanceof GadgetDestruction && Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get()))
                    AbstractGadget.toggleFuzzy(player, stack);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}