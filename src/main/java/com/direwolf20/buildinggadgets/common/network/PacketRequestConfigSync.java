package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.InGameConfig;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRequestConfigSync implements IMessage {
    @Override
    public void fromBytes(ByteBuf byteBuf) {

    }

    @Override
    public void toBytes(ByteBuf byteBuf) {

    }

    public static class Handler implements IMessageHandler<PacketRequestConfigSync,IMessage> {
        /**
         * Called when a message is received of the appropriate type. You can optionally return a reply message, or null if no reply
         * is needed.
         *
         * @param message The message
         * @param ctx
         * @return an optional return message
         */
        @Override
        public IMessage onMessage(PacketRequestConfigSync message, MessageContext ctx) {
            if (ctx.side!= Side.SERVER)
                return null;
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                BuildingGadgets.logger.info("Client requested Config update. Sending config to {}.",ctx.getServerHandler().player.getName());
                InGameConfig.sendConfigUpdateTo(ctx.getServerHandler().player);
            });
            return null;
        }
    }
}
