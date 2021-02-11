package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.old_items.*;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketBindTool {
    public static void encode(PacketBindTool msg, PacketBuffer buffer) { }
    public static PacketBindTool decode(PacketBuffer buffer) {
        return new PacketBindTool();
    }

    public static class Handler {
        public static void handle(final PacketBindTool msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                if (!(stack.getItem() instanceof GadgetDestruction))
                    GadgetUtils.linkToInventory(stack, player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
