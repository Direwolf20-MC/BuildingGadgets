package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRotateMirror {

    public static void encode(PacketRotateMirror msg, PacketBuffer buffer) {}
    public static PacketRotateMirror decode(PacketBuffer buffer) { return new PacketRotateMirror(); }

    public static class Handler {
        public static void handle(final PacketRotateMirror msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = GadgetGeneric.getGadget(player);
                if (stack.getItem() instanceof GadgetBuilding || stack.getItem() instanceof GadgetExchanger)
                    GadgetUtils.rotateOrMirrorToolBlock(stack, player);
                else if (stack.getItem() instanceof GadgetCopyPaste)
                    GadgetCopyPaste.rotateOrMirrorBlocks(stack, player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}