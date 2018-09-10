package com.direwolf20.buildinggadgets.network;

import com.direwolf20.buildinggadgets.tools.PasteToolBufferBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTemplateBlockMap implements IMessage {

    NBTTagCompound tag = new NBTTagCompound();

    @Override
    public void fromBytes(ByteBuf buf) {
        tag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }

    public PacketTemplateBlockMap() {
    }

    public PacketTemplateBlockMap(NBTTagCompound tagCompound) {
        tag = tagCompound.copy();
    }

    public static class Handler implements IMessageHandler<PacketTemplateBlockMap, IMessage> {
        @Override
        public IMessage onMessage(PacketTemplateBlockMap message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketTemplateBlockMap message, MessageContext ctx) {
            if (message.tag.equals(new NBTTagCompound())) {
                PasteToolBufferBuilder.clearMaps();
            }
            String UUID = message.tag.getString("UUID");
            PasteToolBufferBuilder.addToMap(UUID, message.tag);
        }
    }
}
