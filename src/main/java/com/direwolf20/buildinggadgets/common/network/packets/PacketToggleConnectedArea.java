package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleConnectedArea {

    public static void encode(PacketToggleConnectedArea msg, PacketBuffer buffer) {}
    public static PacketToggleConnectedArea decode(PacketBuffer buffer) { return new PacketToggleConnectedArea(); }

    public static class Handler {
        public static void handle(final PacketToggleConnectedArea msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = GadgetGeneric.getGadget(player);
                if (stack.getItem() instanceof GadgetExchanger || stack.getItem() instanceof GadgetBuilding || stack.getItem() instanceof GadgetDestruction)
                    GadgetGeneric.toggleConnectedArea(player, stack);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}