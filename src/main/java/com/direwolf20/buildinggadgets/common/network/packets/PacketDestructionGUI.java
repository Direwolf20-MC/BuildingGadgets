package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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

    public static void encode(PacketDestructionGUI msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.left);
        buffer.writeInt(msg.right);
        buffer.writeInt(msg.up);
        buffer.writeInt(msg.down);
        buffer.writeInt(msg.depth);
    }

    public static PacketDestructionGUI decode(FriendlyByteBuf buffer) {
        return new PacketDestructionGUI(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public static class Handler {
        public static void handle(final PacketDestructionGUI msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ItemStack heldItem = GadgetDestruction.getGadget(ctx.get().getSender());
                if (heldItem.isEmpty()) return;

                GadgetDestruction.setToolValue(heldItem, msg.left, NBTKeys.GADGET_VALUE_LEFT);
                GadgetDestruction.setToolValue(heldItem, msg.right, NBTKeys.GADGET_VALUE_RIGHT);
                GadgetDestruction.setToolValue(heldItem, msg.up, NBTKeys.GADGET_VALUE_UP);
                GadgetDestruction.setToolValue(heldItem, msg.down, NBTKeys.GADGET_VALUE_DOWN);
                GadgetDestruction.setToolValue(heldItem, msg.depth, NBTKeys.GADGET_VALUE_DEPTH);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}