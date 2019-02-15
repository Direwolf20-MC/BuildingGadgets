//package com.direwolf20.buildinggadgets.common.network;
//
//import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerCommands;
//import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
//import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//import net.minecraftforge.fml.common.FMLCommonHandler;
//import net.minecraftforge.fml.common.network.ByteBufUtils;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
//import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
//
//public class PacketTemplateManagerPaste_old implements IMessage {
//
//    private NBTTagCompound tag = new NBTTagCompound();
//    private BlockPos pos;
//
//    @Override
//    public void fromBytes(ByteBuf buf) {
//        System.out.println("Buf size: " + buf.readableBytes());
//        tag = ByteBufUtils.readTag(buf);
//        pos = BlockPos.fromLong(buf.readLong());
//    }
//
//    @Override
//    public void toBytes(ByteBuf buf) {
//        ByteBufUtils.writeTag(buf, tag);
//        buf.writeLong(pos.toLong());
//        System.out.println("Buf size: " + buf.readableBytes());
//    }
//
//    public PacketTemplateManagerPaste_old() {
//    }
//
//    public PacketTemplateManagerPaste_old(NBTTagCompound tagCompound, BlockPos TMpos) {
//        tag = tagCompound.copy();
//        pos = TMpos;
//    }
//
//    public static class Handler implements IMessageHandler<PacketTemplateManagerPaste_old, IMessage> {
//        @Override
//        public IMessage onMessage(PacketTemplateManagerPaste_old message, MessageContext ctx) {
//            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
//            return null;
//        }
//
//        private void handle(PacketTemplateManagerPaste_old message, MessageContext ctx) {
//            if (message.tag.equals(new NBTTagCompound())) {
//                return;
//            }
//            EntityPlayerMP player = ctx.getServerHandler().player;
//            World world = player.world;
//            BlockPos pos = message.pos;
//            TileEntity te = world.getTileEntity(pos);
//            if (!(te instanceof TemplateManagerTileEntity)) return;
//            TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
//            TemplateManagerCommands.pasteTemplate(container, player, message.tag, "");
//        }
//    }
//}
