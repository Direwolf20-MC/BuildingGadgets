package com.direwolf20.buildinggadgets.network;

import com.direwolf20.buildinggadgets.tools.WorldSave;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestBlockMap implements IMessage {

    private String UUID = "";
    private boolean isTemplate;

    @Override
    public void fromBytes(ByteBuf buf) {
        UUID = ByteBufUtils.readUTF8String(buf);
        isTemplate = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, UUID);
        buf.writeBoolean(isTemplate);
    }

    public PacketRequestBlockMap() {
    }

    public PacketRequestBlockMap(String ID, boolean isTemplate) {
        UUID = ID;
        this.isTemplate = isTemplate;
    }

    public static class Handler implements IMessageHandler<PacketRequestBlockMap, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestBlockMap message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestBlockMap message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            NBTTagCompound tagCompound = (message.isTemplate ? WorldSave.getTemplateWorldSave(player.world) : WorldSave.getWorldSave(player.world)).getCompoundFromUUID(message.UUID);
            if (tagCompound != null) {
                PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), player);
                //System.out.println("Sending BlockMap Packet");
            }
        }
    }
}
