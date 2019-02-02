package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet containing a {@link NBTTagCompound} representing the serialized Config Data, to be updated on the Client.
 */
public class PacketSyncConfig {
    private final NBTTagCompound compound;

    public PacketSyncConfig(NBTTagCompound compound) {
        this.compound = compound;
    }

    public static void encode(PacketSyncConfig msg, PacketBuffer buffer) {
        buffer.writeCompoundTag(msg.compound);
    }

    public static PacketSyncConfig decode(PacketBuffer buffer) {
        return new PacketSyncConfig(buffer.readCompoundTag());
    }

    // Why would you do this?
    public NBTTagCompound getTag() {
        return this.compound;
    }

    /**
     * Client-Side Handler for {@link PacketSyncConfig}
     */
    public static class Handler {
        public static void handle(final PacketSyncConfig msg, Supplier<NetworkEvent.Context> ctx) {
            if( ctx.get().getDirection() != NetworkDirection.PLAY_TO_SERVER )
                return;

            ctx.get().enqueueWork(() -> {
                NBTTagCompound compound = msg.getTag();
                BuildingGadgets.LOG.info("Received SyncedConfig from Server.");
                SyncedConfig.onReadSynchronisation(compound);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
