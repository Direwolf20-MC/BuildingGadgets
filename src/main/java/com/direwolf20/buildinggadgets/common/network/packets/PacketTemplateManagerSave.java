package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerCommands;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketTemplateManagerSave {

    private final BlockPos pos;
    private final String name;

    public PacketTemplateManagerSave(BlockPos blockPos, String TemplateName) {
        this.pos = blockPos;
        this.name = TemplateName;
    }

    public static void encode(PacketTemplateManagerSave msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.pos);
        buffer.writeString(msg.name);
    }

    public static PacketTemplateManagerSave decode(PacketBuffer buffer) {
        return new PacketTemplateManagerSave(buffer.readBlockPos(), buffer.readString(125));
    }

    public static class Handler {
        public static void handle(PacketTemplateManagerSave msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if( player == null ) return;

                World world = player.world;
                BlockPos pos = msg.pos;

                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof TemplateManagerTileEntity)) return;
                TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);

                TemplateManagerCommands.saveTemplate(container, player, msg.name);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
