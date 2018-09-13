package com.direwolf20.buildinggadgets.network;

import com.direwolf20.buildinggadgets.blocks.templatemanager.TemplateManagerCommands;
import com.direwolf20.buildinggadgets.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.blocks.templatemanager.TemplateManagerTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class PacketTemplateManagerPaste implements IMessage {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        //System.out.println("Buf size: " + buf.readableBytes());
        buf.readBytes(baos.toByteArray());
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeBytes(baos.toByteArray());
        //System.out.println("Buf size: " + buf.readableBytes());
    }

    public PacketTemplateManagerPaste() {
    }

    public PacketTemplateManagerPaste(NBTTagCompound tagCompound, BlockPos TMpos, Boolean first, Boolean last) {
        try {
            CompressedStreamTools.writeCompressed(tagCompound, baos);
            pos = TMpos;
        } catch (Throwable t) {
        }

    }

    public static class Handler implements IMessageHandler<PacketTemplateManagerPaste, IMessage> {
        @Override
        public IMessage onMessage(PacketTemplateManagerPaste message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketTemplateManagerPaste message, MessageContext ctx) {
            ByteArrayInputStream bais = new ByteArrayInputStream(message.baos.toByteArray());
            try {
                NBTTagCompound newTag = CompressedStreamTools.readCompressed(bais);
                if (newTag.equals(new NBTTagCompound())) return;

                EntityPlayerMP player = ctx.getServerHandler().player;
                World world = player.world;
                BlockPos pos = message.pos;
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof TemplateManagerTileEntity)) return;
                TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
                TemplateManagerCommands.PasteTemplate(container, player, newTag);
            } catch (Throwable t) {
            }


        }
    }
}
