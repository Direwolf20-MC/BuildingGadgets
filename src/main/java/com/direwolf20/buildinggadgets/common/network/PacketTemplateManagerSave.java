package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTemplateManagerSave implements IMessage {

    private BlockPos pos;
    private String name;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buf, name);
    }

    public PacketTemplateManagerSave() {
    }

    public PacketTemplateManagerSave(BlockPos blockPos, String TemplateName) {
        pos = blockPos;
        name = TemplateName;
    }

    public static class Handler implements IMessageHandler<PacketTemplateManagerSave, IMessage> {
        @Override
        public IMessage onMessage(PacketTemplateManagerSave message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketTemplateManagerSave message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            World world = player.world;
            BlockPos pos = message.pos;
            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof TemplateManagerTileEntity)) return;
            TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
            container.saveTemplate(player, message.name);


        }
    }
}
