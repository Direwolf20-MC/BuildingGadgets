package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.api.WorldSave;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PacketRequestBlockMap implements IMessage {

    private UUID uuid = UUID.randomUUID();
    private boolean isTemplate;

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = new UUID(buf.readLong(),buf.readLong());
        isTemplate = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        //write it in the order the constructor expects it
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
        buf.writeBoolean(isTemplate);
    }

    public PacketRequestBlockMap() {
    }

    public PacketRequestBlockMap(UUID ID, boolean isTemplate) {
        uuid = ID;
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
            NBTTagCompound tagCompound = (message.isTemplate ? WorldSave.getTemplateWorldSave(player.world) : WorldSave.getWorldSave(player.world)).getCompoundFromUUID(message.uuid);
            if (tagCompound != null) {
                PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), player);
                //System.out.println("Sending BlockMap Packet");
            }
        }
    }
}
