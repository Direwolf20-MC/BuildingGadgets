package com.direwolf20.buildinggadgets.common.network.packets;

import java.util.function.Supplier;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAnchorKey {

    public static void encode(PacketAnchorKey msg, PacketBuffer buffer) {}
    public static PacketAnchorKey decode(PacketBuffer buffer) { return null; }

    public static class Handler {
        public static void handle(final PacketAnchorKey msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = GadgetGeneric.getGadget(player);
                if (stack.getItem() instanceof GadgetBuilding)
                    GadgetUtils.anchorBlocks(player, stack);
                else if (stack.getItem() instanceof GadgetExchanger)
                    GadgetUtils.anchorBlocks(player, stack);
                else if (stack.getItem() instanceof GadgetCopyPaste)
                    GadgetCopyPaste.anchorBlocks(player, stack);
                else if (stack.getItem() instanceof GadgetDestruction)
                    GadgetDestruction.anchorBlocks(player, stack);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}