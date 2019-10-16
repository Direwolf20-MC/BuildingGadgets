package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTemplateManagerLoad {

    private final BlockPos pos;

    public PacketTemplateManagerLoad(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(PacketTemplateManagerLoad msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.pos);
    }

    public static PacketTemplateManagerLoad decode(PacketBuffer buffer) {
        return new PacketTemplateManagerLoad(buffer.readBlockPos());
    }

    public static class Handler {
        public static void handle(PacketTemplateManagerLoad msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if( player == null ) return;

                World world = player.world;
                BlockPos pos = msg.pos;

                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof TemplateManagerTileEntity)) return;

                TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
                //TemplateManagerCommands.loadTemplate(container, player);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
