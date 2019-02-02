package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This empty packets represents a Request from the Client to re-send the {@link SyncedConfig}.
 */
public class PacketRequestConfigSync {

    public static void encode(PacketRequestConfigSync msg, PacketBuffer buf) {}
    public static PacketRequestConfigSync decode(PacketBuffer buf) { return null; }

    /**
     * Server-Side Handler for {@link PacketRequestConfigSync}
     */
    public static class Handler {
        public static void handle(PacketRequestConfigSync message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP player = ctx.get().getSender();
                if( player == null ) return;

                BuildingGadgets.LOG.info("Client requested Config update. Sending config to {}.", player.getName().getString());
                SyncedConfig.sendConfigUpdateTo(player);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
