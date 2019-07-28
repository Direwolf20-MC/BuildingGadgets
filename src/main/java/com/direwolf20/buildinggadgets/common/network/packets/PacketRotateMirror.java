package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PacketRotateMirror {
    private Operation operation;

    public PacketRotateMirror() {}

    public PacketRotateMirror(@Nullable Operation operation) {
        this.operation = operation;
    }

    public static void encode(PacketRotateMirror msg, PacketBuffer buffer) {
        boolean hasOperation = msg.operation != null;
        buffer.writeBoolean(hasOperation);
        if (hasOperation)
            buffer.writeInt(msg.operation.ordinal());
    }

    public static PacketRotateMirror decode(PacketBuffer buffer) {
        return new PacketRotateMirror(buffer.readBoolean() ? Operation.values()[buffer.readInt()] : null);
    }

    public static enum Operation {
        ROTATE, MIRROR;
    }

    public static class Handler {
        public static void handle(final PacketRotateMirror msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player == null)
                    return;

                ItemStack stack = AbstractGadget.getGadget(player);
                Operation operation = msg.operation != null ? msg.operation : (player.isSneaking() ? Operation.MIRROR : Operation.ROTATE);
                if (stack.getItem() instanceof GadgetBuilding || stack.getItem() instanceof GadgetExchanger)
                    GadgetUtils.rotateOrMirrorToolBlock(stack, player, operation);
                else if (stack.getItem() instanceof GadgetCopyPaste)
                    GadgetCopyPaste.rotateOrMirrorBlocks(stack, player, operation);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}