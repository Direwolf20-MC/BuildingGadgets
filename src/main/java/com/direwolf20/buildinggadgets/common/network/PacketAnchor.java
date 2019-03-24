package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketAnchor extends PacketEmpty {

    public static class Handler implements IMessageHandler<PacketAnchor, IMessage> {
        @Override
        public IMessage onMessage(PacketAnchor message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(ctx));
            return null;
        }

        private void handle(MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;
            ItemStack heldItem = GadgetGeneric.getGadget(playerEntity);
            if (heldItem.isEmpty())
                return;

            if (heldItem.getItem() instanceof GadgetBuilding) {
                GadgetUtils.anchorBlocks(playerEntity, heldItem);
            } else if (heldItem.getItem() instanceof GadgetExchanger) {
                GadgetUtils.anchorBlocks(playerEntity, heldItem);
            } else if (heldItem.getItem() instanceof GadgetCopyPaste) {
                GadgetCopyPaste.anchorBlocks(playerEntity, heldItem);
            } else if (heldItem.getItem() instanceof GadgetDestruction) {
                GadgetDestruction.anchorBlocks(playerEntity, heldItem);
            }
        }
    }
}