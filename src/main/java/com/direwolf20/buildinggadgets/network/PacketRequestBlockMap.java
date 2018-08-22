package com.direwolf20.buildinggadgets.network;

import com.direwolf20.buildinggadgets.tools.BlockMapWorldSave;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestBlockMap implements IMessage {

    String UUID = "";

    @Override
    public void fromBytes(ByteBuf buf) {
        UUID = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, UUID);
    }

    public PacketRequestBlockMap() {
    }

    public PacketRequestBlockMap(String ID) {
        UUID = ID;
    }

    public static class Handler implements IMessageHandler<PacketRequestBlockMap, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestBlockMap message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestBlockMap message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            World world = player.world;
            BlockMapWorldSave worldSave = BlockMapWorldSave.get(world);
            NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(message.UUID);
            PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);
        }
    }
}
