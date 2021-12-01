package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleMode {
    private final int mode;

    public static void encode(PacketToggleMode msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.mode);
    }
    public static PacketToggleMode decode(FriendlyByteBuf buffer) {
        return new PacketToggleMode(buffer.readInt());
    }

    public PacketToggleMode(int mode) {
        this.mode = mode;
    }

    public static class Handler {
        public static void handle(PacketToggleMode msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer playerEntity = ctx.get().getSender();
                if( playerEntity == null ) return;

                ItemStack heldItem = AbstractGadget.getGadget(playerEntity);
                if (heldItem.isEmpty())
                    return;

                if (heldItem.getItem() instanceof GadgetBuilding) {
                    GadgetBuilding gadgetBuilding = (GadgetBuilding) (heldItem.getItem());
                    gadgetBuilding.setMode(heldItem, msg.mode);
                } else if (heldItem.getItem() instanceof GadgetExchanger) {
                    GadgetExchanger gadgetExchanger = (GadgetExchanger) (heldItem.getItem());
                    gadgetExchanger.setMode(heldItem, msg.mode);
                } else if (heldItem.getItem() instanceof GadgetCopyPaste) {
                    GadgetCopyPaste gadgetCopyPaste = (GadgetCopyPaste) (heldItem.getItem());
                    gadgetCopyPaste.setMode(heldItem, msg.mode);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
