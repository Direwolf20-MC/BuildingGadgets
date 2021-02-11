package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.old_items.GadgetCopyPaste;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketPasteGUI {

    private final int X, Y, Z;

    public PacketPasteGUI(int x, int y, int z) {
        X = x;
        Y = y;
        Z = z;
    }

    public static void encode(PacketPasteGUI msg, PacketBuffer buffer) {
        buffer.writeInt(msg.X);
        buffer.writeInt(msg.Y);
        buffer.writeInt(msg.Z);
    }

    public static PacketPasteGUI decode(PacketBuffer buffer) {
        return new PacketPasteGUI(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public static class Handler {
        public static void handle(PacketPasteGUI msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ItemStack heldItem = GadgetCopyPaste.getGadget(ctx.get().getSender());
                if (heldItem.isEmpty()) return;
                GadgetCopyPaste.setRelativeVector(heldItem, new BlockPos(msg.X, msg.Y, msg.Z));
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
