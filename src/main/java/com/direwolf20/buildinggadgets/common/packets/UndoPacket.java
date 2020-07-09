package com.direwolf20.buildinggadgets.common.packets;

import com.direwolf20.buildinggadgets.common.items.Gadget;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class UndoPacket {

    public static void encode(UndoPacket msg, PacketBuffer buffer) { }
    public static UndoPacket decode(PacketBuffer buffer) {
        return new UndoPacket();
    }

    public static class Handler {
        public static void handle(final UndoPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player == null)
                    return;

                Optional<ItemStack> stack = Gadget.findGadget(player);
                stack.ifPresent(gadget -> ((Gadget) gadget.getItem()).undo(gadget, player.world, player));
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
