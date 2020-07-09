package com.direwolf20.buildinggadgets.common.packets;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.BuildingGadget;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Register and handle our packets
 */
public class Packets {

    // Setup our Protocol
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BuildingGadgets.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    /**
     * Register the packets
     */
    public static void register() {
        int id = 0;

        INSTANCE.registerMessage(id++, SetRangePacket.class, SetRangePacket::encode, SetRangePacket::decode, SetRangePacket.Handler::handle);
        INSTANCE.registerMessage(id++, SetModePacket.class, SetModePacket::encode, SetModePacket::decode, SetModePacket.Handler::handle);
    }

    public static void sendTo(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer))
            INSTANCE.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object msg) {
        INSTANCE.sendToServer(msg);
    }

    public static void send(Object msg, PacketDistributor.PacketTarget target) {
        INSTANCE.send(target, msg);
    }

}
