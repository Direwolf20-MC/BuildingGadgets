package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        buffer.writeByteArray(msg.data);
        buffer.writeBlockPos(msg.pos);
        buffer.writeString(msg.templateName, 125);
    }

    public static PacketTemplateManagerPaste decode(PacketBuffer buffer) {
        return new PacketTemplateManagerPaste(buffer.readByteArray(), buffer.readBlockPos(), buffer.readString(125));
    }


    public static class Handler {
        public static void handle(PacketTemplateManagerPaste msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(msg.data);
                try {
                    CompoundNBT newTag = CompressedStreamTools.readCompressed(inputStream);
                    if (newTag.equals(new CompoundNBT())) return;

                    ServerPlayerEntity player = ctx.get().getSender();
                    if( player == null ) return;

                    World world = player.world;
                    BlockPos pos = msg.pos;
                    TileEntity te = world.getTileEntity(pos);
                    if (!(te instanceof TemplateManagerTileEntity)) return;

                    TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
                    //TemplateManagerCommands.pasteTemplate(container, player, newTag, msg.templateName);
                } catch (IOException e) {
                    BuildingGadgets.LOG.error("TemplateItem Manager paste failed", e);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
