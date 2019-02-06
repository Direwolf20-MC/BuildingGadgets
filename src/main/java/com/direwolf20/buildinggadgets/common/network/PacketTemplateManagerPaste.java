package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerCommands;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class PacketTemplateManagerPaste implements IMessage {

    //ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private BlockPos pos;
    private byte[] data;
    private String templateName;

    @Override
    public void fromBytes(ByteBuf buf) {
        //System.out.println("Buf size: " + buf.readableBytes());
        pos = BlockPos.fromLong(buf.readLong());
        templateName = ByteBufUtils.readUTF8String(buf);
        data = new byte[buf.readableBytes()];
        buf.readBytes(data);

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buf, templateName);
        buf.writeBytes(data);

        //System.out.println("Buf size: " + buf.readableBytes());
    }

    public PacketTemplateManagerPaste() {
    }

    public PacketTemplateManagerPaste(ByteArrayOutputStream pasteStream, BlockPos TMpos, String name) {
        pos = TMpos;
        data = pasteStream.toByteArray();
        templateName = name;
    }

    public static class Handler implements IMessageHandler<PacketTemplateManagerPaste, IMessage> {
        @Override
        public IMessage onMessage(PacketTemplateManagerPaste message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketTemplateManagerPaste message, MessageContext ctx) {
            ByteArrayInputStream bais = new ByteArrayInputStream(message.data);
            try {
                NBTTagCompound newTag = CompressedStreamTools.readCompressed(bais);
                if (newTag.equals(new NBTTagCompound())) return;

                EntityPlayerMP player = ctx.getServerHandler().player;
                World world = player.world;
                BlockPos pos = message.pos;
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof TemplateManagerTileEntity)) return;
                TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
                TemplateManagerCommands.pasteTemplate(container, player, newTag, message.templateName);
            } catch (Throwable t) {
                System.out.println(t);
            }


        }
    }
}
