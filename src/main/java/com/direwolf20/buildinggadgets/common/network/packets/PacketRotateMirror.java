package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PacketRotateMirror {
    private Operation operation;

    public PacketRotateMirror() {}

    public PacketRotateMirror(@Nullable Operation operation) {
        this.operation = operation;
    }

    public static void encode(PacketRotateMirror msg, FriendlyByteBuf buffer) {
        boolean hasOperation = msg.operation != null;
        buffer.writeBoolean(hasOperation);
        if (hasOperation)
            buffer.writeInt(msg.operation.ordinal());
    }

    public static PacketRotateMirror decode(FriendlyByteBuf buffer) {
        return new PacketRotateMirror(buffer.readBoolean() ? Operation.values()[buffer.readInt()] : null);
    }

    public enum Operation {
        ROTATE, MIRROR;
    }

    public static class Handler {
        public static void handle(final PacketRotateMirror msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                Operation operation = msg.operation != null ? msg.operation : (player.isShiftKeyDown() ? Operation.MIRROR : Operation.ROTATE);

                if (operation == Operation.MIRROR) {
                    ((AbstractGadget) stack.getItem()).onMirror(stack, player);
                } else {
                    ((AbstractGadget) stack.getItem()).onRotate(stack, player);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}