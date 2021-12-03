package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketBindTool {
    public static void encode(PacketBindTool msg, FriendlyByteBuf buffer) { }
    public static PacketBindTool decode(FriendlyByteBuf buffer) {
        return new PacketBindTool();
    }

    public static class Handler {
        public static void handle(final PacketBindTool msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
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