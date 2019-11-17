package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.gadgets.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUndo extends PacketEmpty {

    public static class Handler implements IMessageHandler<PacketUndo, IMessage> {
        @Override
        public IMessage onMessage(PacketUndo message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(ctx));
            return null;
        }

        private void handle(MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack stack = GadgetGeneric.getGadget(player);
            GadgetGeneric item = (GadgetGeneric) stack.getItem();
            if (item instanceof GadgetBuilding)
                ((GadgetBuilding) item).undoBuild(player);
            else if (item instanceof GadgetCopyPaste)
                ((GadgetCopyPaste) item).undoBuild(player, stack);
            else if (item instanceof GadgetDestruction)
                ((GadgetDestruction) item).undo(player, stack);
        }
    }
}
