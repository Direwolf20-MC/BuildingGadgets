package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketToggleMode {
    private final int mode;

    public static void encode(PacketToggleMode msg, PacketBuffer buffer) {
        buffer.writeInt(msg.mode);
    }
    public static PacketToggleMode decode(PacketBuffer buffer) {
        return new PacketToggleMode(buffer.readInt());
    }

    public PacketToggleMode(int mode) {
        this.mode = mode;
    }

    public static class Handler {
        public static void handle(PacketToggleMode msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity playerEntity = ctx.get().getSender();
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
