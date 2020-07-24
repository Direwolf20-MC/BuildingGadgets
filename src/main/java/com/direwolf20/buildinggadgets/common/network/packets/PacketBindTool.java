package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
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
                if (stack.getItem() instanceof GadgetBuilding)
                    GadgetUtils.bindToolToTE(stack, player);
                else if (stack.getItem() instanceof GadgetExchanger)
                    GadgetUtils.bindToolToTE(stack, player);
                else if (stack.getItem() instanceof GadgetCopyPaste)
                    GadgetUtils.bindToolToTE(stack, player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}