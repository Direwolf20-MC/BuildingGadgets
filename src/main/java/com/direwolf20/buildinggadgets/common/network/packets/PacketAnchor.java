package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.old_items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.old_items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.old_items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.entity.player.PlayerEntity;
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
