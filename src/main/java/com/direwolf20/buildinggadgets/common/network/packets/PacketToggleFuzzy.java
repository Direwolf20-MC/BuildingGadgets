package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleFuzzy {

    public static void encode(PacketToggleFuzzy msg, PacketBuffer buffer) {}
    public static PacketToggleFuzzy decode(PacketBuffer buffer) { return new PacketToggleFuzzy(); }

    public static class Handler {
        public static void handle(final PacketToggleFuzzy msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = GadgetGeneric.getGadget(player);
                if (stack.getItem() instanceof GadgetExchanger || stack.getItem() instanceof GadgetBuilding
                        || (stack.getItem() instanceof GadgetDestruction && Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get()))
                    GadgetGeneric.toggleFuzzy(player, stack);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}