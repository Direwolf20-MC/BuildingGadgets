package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketChangeRange extends PacketEmpty {

    public static class Handler implements IMessageHandler<PacketChangeRange, IMessage> {
        @Override
        public IMessage onMessage(PacketChangeRange message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(ctx));
            return null;
        }

        private void handle(MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
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
        }
    }
}