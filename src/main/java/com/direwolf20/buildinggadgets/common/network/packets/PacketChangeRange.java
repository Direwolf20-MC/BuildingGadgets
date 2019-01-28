package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangeRange {

    public PacketChangeRange() {}
    public static void encode(PacketChangeRange msg, PacketBuffer buffer) {}
    public static PacketChangeRange decode(PacketBuffer buffer) { return null; }

    public static class Handler {
//        @Override
//        public IMessage onMessage(PacketChangeRange message, MessageContext ctx) {
//            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(ctx));
//            return null;
//        }

        public static void handle(final PacketChangeRange msg, Supplier<NetworkEvent.Context> ctx) {
            EntityPlayerMP playerEntity = ctx.get().getSender();
            if( playerEntity == null )
                return;

            ctx.get().enqueueWork(() -> {
                ItemStack heldItem = GadgetGeneric.getGadget(playerEntity);
                if (heldItem.isEmpty())
                    return;

                if (heldItem.getItem() instanceof GadgetBuilding) {
                    GadgetBuilding gadgetBuilding = (GadgetBuilding) (heldItem.getItem());
                    gadgetBuilding.rangeChange(playerEntity, heldItem);
                } else if (heldItem.getItem() instanceof GadgetExchanger) {
                    GadgetExchanger gadgetExchanger = (GadgetExchanger) (heldItem.getItem());
                    gadgetExchanger.rangeChange(playerEntity, heldItem);
                } else if (heldItem.getItem() instanceof GadgetCopyPaste) {
                    GadgetCopyPaste gadgetCopyPaste = (GadgetCopyPaste) (heldItem.getItem());
                    gadgetCopyPaste.rotateBlocks(heldItem, playerEntity);
                } else if (heldItem.getItem() instanceof GadgetDestruction) {
                    GadgetDestruction gadgetDestruction = (GadgetDestruction) (heldItem.getItem());
                    gadgetDestruction.switchOverlay(heldItem);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}