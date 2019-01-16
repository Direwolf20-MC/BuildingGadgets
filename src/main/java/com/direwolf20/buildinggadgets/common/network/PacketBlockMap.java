package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.api.ITemplateOld;
import com.direwolf20.buildinggadgets.common.tools.PasteToolBufferBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PacketBlockMap implements IMessage {

    private NBTTagCompound tag = new NBTTagCompound();

    @Override
    public void fromBytes(ByteBuf buf) {
        tag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }

    public PacketBlockMap() {
    }

    public PacketBlockMap(NBTTagCompound tagCompound) {
        tag = tagCompound.copy();
    }

    public static class Handler implements IMessageHandler<PacketBlockMap, IMessage> {
        @Override
        public IMessage onMessage(PacketBlockMap message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message));
            return null;
        }

        private void handle(PacketBlockMap message) {
            if (message.tag.equals(new NBTTagCompound())) {
                PasteToolBufferBuilder.clearMaps();
            }
            UUID uuid = message.tag.getUniqueId("UUID");
            if (uuid == null || uuid.equals(ITemplateOld.INVALID_UUID)) {
                uuid = UUID.fromString(message.tag.getString("UUID"));
            }
            PasteToolBufferBuilder.addToMap(uuid, message.tag);
            //System.out.println("Sent blockmap for: " + UUID);
        }
    }
}
