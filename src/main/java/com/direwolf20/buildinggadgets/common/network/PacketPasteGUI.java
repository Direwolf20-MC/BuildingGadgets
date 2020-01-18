package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.gadgets.CopyGadget;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPasteGUI implements IMessage {

    int X, Y, Z;

    @Override
    public void fromBytes(ByteBuf buf) {
        X = buf.readInt();
        Y = buf.readInt();
        Z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(X);
        buf.writeInt(Y);
        buf.writeInt(Z);
    }

    public PacketPasteGUI() {

    }

    public PacketPasteGUI(int x, int y, int z) {
        X = x;
        Y = y;
        Z = z;
    }

    public static class Handler implements IMessageHandler<PacketPasteGUI, IMessage> {
        @Override
        public IMessage onMessage(PacketPasteGUI message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketPasteGUI message, MessageContext ctx) {
            EntityPlayerMP playerEntity = ctx.getServerHandler().player;

            ItemStack heldItem = CopyGadget.getAsStack(playerEntity);
            if (heldItem.isEmpty()) return;

            CopyGadget.setX(heldItem, message.X);
            CopyGadget.setY(heldItem, message.Y);
            CopyGadget.setZ(heldItem, message.Z);
        }
    }
}