package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketAnchor {
    public static void encode(PacketAnchor msg, FriendlyByteBuf buffer) {}
    public static PacketAnchor decode(FriendlyByteBuf buffer) { return new PacketAnchor(); }

    public static class Handler {
        public static void handle(final PacketAnchor msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                if (stack.getItem() instanceof GadgetBuilding)
                    GadgetUtils.anchorBlocks(player, stack);
                else if (stack.getItem() instanceof GadgetExchanger)
                    GadgetUtils.anchorBlocks(player, stack);
                else
                    ((AbstractGadget) stack.getItem()).onAnchor(stack, player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}