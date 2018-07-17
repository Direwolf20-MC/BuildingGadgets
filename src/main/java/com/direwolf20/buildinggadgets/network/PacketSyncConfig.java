package com.direwolf20.buildinggadgets.network;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.config.InGameConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketSyncConfig implements IMessage {
    private NBTTagCompound tagCompound;
    public PacketSyncConfig() {
        this(null);
    }

    public PacketSyncConfig(NBTTagCompound tagCompound) {
        this.tagCompound = tagCompound;
    }

    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        tagCompound = ByteBufUtils.readTag(buf);
    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf,tagCompound);
    }

    public NBTTagCompound getTagCompound() {
        return tagCompound;
    }

    public static class Handler implements IMessageHandler<PacketSyncConfig,IMessage> {
        /**
         * Called when a message is received of the appropriate type. You can optionally return a reply message, or null if no reply
         * is needed.
         *
         * @param message The message
         * @param ctx
         * @return an optional return message
         */
        @Override
        public IMessage onMessage(PacketSyncConfig message, MessageContext ctx) {
            if (ctx.side!=Side.CLIENT)
                return null;
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    NBTTagCompound compound = message.getTagCompound();
                    BuildingGadgets.logger.info("Received InGameConfig from Server.");
                    InGameConfig.onReadSynchronisation(compound);
                }
            });
            return null;
        }
    }
}
