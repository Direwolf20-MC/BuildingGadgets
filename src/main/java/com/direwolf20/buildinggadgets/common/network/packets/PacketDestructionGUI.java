package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDestructionGUI {

    private final int left, right, up, down, depth;

    public PacketDestructionGUI(int l, int r, int u, int d, int dep) {
        left = l;
        right = r;
        up = u;
        down = d;
        depth = dep;
    }

    public static void encode(PacketDestructionGUI msg, PacketBuffer buffer) {
        buffer.writeInt(msg.left);
        buffer.writeInt(msg.right);
        buffer.writeInt(msg.up);
        buffer.writeInt(msg.down);
        buffer.writeInt(msg.depth);
    }

    public static PacketDestructionGUI decode(PacketBuffer buffer) {
        return new PacketDestructionGUI(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public static class Handler {
//        @Override
//        public IMessage onMessage(PacketDestructionGUI message, MessageContext ctx) {
//            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
//            return null;
//        }

        public static void handle(final PacketDestructionGUI msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ItemStack heldItem = GadgetDestruction.getGadget(ctx.get().getSender());
                if (heldItem.isEmpty()) return;

                GadgetDestruction.setToolValue(heldItem, msg.left, "left");
                GadgetDestruction.setToolValue(heldItem, msg.right, "right");
                GadgetDestruction.setToolValue(heldItem, msg.up, "up");
                GadgetDestruction.setToolValue(heldItem, msg.down, "down");
                GadgetDestruction.setToolValue(heldItem, msg.depth, "depth");
            });

            ctx.get().setPacketHandled(true);
        }
    }
}