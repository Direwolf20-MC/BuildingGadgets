package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerCommands;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Supplier;

public class PacketTemplateManagerPaste {

    private final BlockPos pos;
    private final byte[] data;
    private final String templateName;

    public PacketTemplateManagerPaste(ByteArrayOutputStream pasteStream, BlockPos pos, String name) {
        this(pasteStream.toByteArray(), pos, name);
    }

    public PacketTemplateManagerPaste(byte[] data, BlockPos pos, String name) {
        this.pos = pos;
        this.data = data;
        this.templateName = name;
    }

    public static void encode(PacketTemplateManagerPaste msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeBytes(msg.data);
        buffer.writeString(msg.templateName);
    }

    public static PacketTemplateManagerPaste decode(PacketBuffer buffer) {
        return new PacketTemplateManagerPaste(buffer.readByteArray(), buffer.readBlockPos(), buffer.readString(125));
    }


    public static class Handler {
        public static void handle(PacketTemplateManagerPaste msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(msg.data);
                try {
                    NBTTagCompound newTag = CompressedStreamTools.readCompressed(inputStream);
                    if (newTag.equals(new NBTTagCompound())) return;

                    EntityPlayerMP player = ctx.get().getSender();
                    if( player == null ) return;

                    World world = player.world;
                    BlockPos pos = msg.pos;
                    TileEntity te = world.getTileEntity(pos);
                    if (!(te instanceof TemplateManagerTileEntity)) return;

                    TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
                    TemplateManagerCommands.pasteTemplate(container, player, newTag, msg.templateName);
                } catch (Throwable t) { System.out.println(t); }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
