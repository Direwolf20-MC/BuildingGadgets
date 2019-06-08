package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketAnchor {

    public static void encode(PacketAnchor msg, PacketBuffer buffer) {}
    public static PacketAnchor decode(PacketBuffer buffer) { return new PacketAnchor(); }

    public static class Handler {
        public static void handle(final PacketAnchor msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity player = ctx.get().getSender();
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