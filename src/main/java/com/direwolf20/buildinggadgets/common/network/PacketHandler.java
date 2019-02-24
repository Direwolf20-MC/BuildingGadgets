package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.network.packets.*;
import com.direwolf20.buildinggadgets.common.utils.Reference;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(2);
    private static short index = 0;

    public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Reference.MODID, "main_network_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        // Server side
        registerMessage(PacketAnchorKey.class, PacketAnchorKey::encode, PacketAnchorKey::decode, PacketAnchorKey.Handler::handle);
        registerMessage(PacketBlockMap.class, PacketBlockMap::encode, PacketBlockMap::decode, PacketBlockMap.Handler::handle);
        registerMessage(PacketToggleFuzzy.class, PacketToggleFuzzy::encode, PacketToggleFuzzy::decode, PacketToggleFuzzy.Handler::handle);
        registerMessage(PacketToggleConnectedArea.class, PacketToggleConnectedArea::encode, PacketToggleConnectedArea::decode, PacketToggleConnectedArea.Handler::handle);
        registerMessage(PacketChangeRange.class, PacketChangeRange::encode, PacketChangeRange::decode, PacketChangeRange.Handler::handle);
        registerMessage(PacketCopyCoords.class, PacketCopyCoords::encode, PacketCopyCoords::decode, PacketCopyCoords.Handler::handle);
        registerMessage(PacketDestructionGUI.class, PacketDestructionGUI::encode, PacketDestructionGUI::decode, PacketDestructionGUI.Handler::handle);
        registerMessage(PacketPasteGUI.class, PacketPasteGUI::encode, PacketPasteGUI::decode, PacketPasteGUI.Handler::handle);
        registerMessage(PacketTemplateManagerLoad.class, PacketTemplateManagerLoad::encode, PacketTemplateManagerLoad::decode, PacketTemplateManagerLoad.Handler::handle);
        registerMessage(PacketTemplateManagerPaste.class, PacketTemplateManagerPaste::encode, PacketTemplateManagerPaste::decode, PacketTemplateManagerPaste.Handler::handle);
        registerMessage(PacketTemplateManagerSave.class, PacketTemplateManagerSave::encode, PacketTemplateManagerSave::decode, PacketTemplateManagerSave.Handler::handle);
        registerMessage(PacketToggleMode.class, PacketToggleMode::encode, PacketToggleMode::decode, PacketToggleMode.Handler::handle);
        registerMessage(PacketUndoKey.class, PacketUndoKey::encode, PacketUndoKey::decode, PacketUndoKey.Handler::handle);

        // Client side
        registerMessage(PacketRequestBlockMap.class, PacketRequestBlockMap::encode, PacketRequestBlockMap::decode, PacketRequestBlockMap.Handler::handle);

        // Both Sides
        registerMessage(PacketSetRemoteInventoryCache.class, PacketSetRemoteInventoryCache::encode, PacketSetRemoteInventoryCache::decode, PacketSetRemoteInventoryCache.Handler::handle);
    }

    public static void sendTo(Object msg, EntityPlayerMP player) {
        if (!(player instanceof FakePlayer))
            HANDLER.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<Context>> messageConsumer) {
        HANDLER.registerMessage(index, messageType, encoder, decoder, messageConsumer);
        index++;
        if (index > 0xFF)
            throw new RuntimeException("Too many messages!");
    }
}
