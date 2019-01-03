package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This empty packets represents a Request from the Server to re-send the {@link SyncedConfig}.
 */
public class PacketRequestConfigSync implements IMessage {
    @Override
    public void fromBytes(ByteBuf byteBuf) {

    }

    @Override
    public void toBytes(ByteBuf byteBuf) {

    }

    /**
     * Server-Side Handler for {@link PacketRequestConfigSync}
     */
    public static class Handler implements IMessageHandler<PacketRequestConfigSync,IMessage> {

        @Override
        public IMessage onMessage(PacketRequestConfigSync message, MessageContext ctx) {
            if (ctx.side!= Side.SERVER)
                return null;
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                BuildingGadgets.logger.info("Client requested Config update. Sending config to {}.",ctx.getServerHandler().player.getName());
                SyncedConfig.sendConfigUpdateTo(ctx.getServerHandler().player);
            });
            return null;
        }
    }
}
