package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.network.packets.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = Integer.toString(1);

    public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(BuildingGadgets.MODID, "networker_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {

        HANDLER.registerMessage(1,  PacketAnchorKey.class, PacketAnchorKey::encode, PacketAnchorKey::decode, PacketAnchorKey.Handler::handle);

        // Client side
        HANDLER.registerMessage(2,  PacketBlockMap.class, PacketBlockMap::encode, PacketBlockMap::decode, PacketBlockMap.Handler::handler);

        HANDLER.registerMessage(3,  PacketChangeRange.class, PacketChangeRange::encode, PacketChangeRange::decode, PacketChangeRange.Handler::handle);
        HANDLER.registerMessage(4,  PacketCopyCoords.class, PacketCopyCoords::encode, PacketCopyCoords::decode, PacketCopyCoords.Handler::handle);
        HANDLER.registerMessage(5,  PacketDestructionGUI.class, PacketDestructionGUI::encode, PacketDestructionGUI::decode, PacketDestructionGUI.Handler::handle);
        HANDLER.registerMessage(6,  PacketPasteGUI.class, PacketPasteGUI::encode, PacketPasteGUI::decode, PacketPasteGUI.Handler::handle);
        HANDLER.registerMessage(7,  PacketRequestBlockMap.class, PacketRequestBlockMap::encode, PacketRequestBlockMap::decode, PacketRequestBlockMap.Handler::handle);
        HANDLER.registerMessage(8,  PacketRequestConfigSync.class, PacketRequestConfigSync::encode, PacketRequestConfigSync::decode, PacketRequestConfigSync.Handler::handle);

        // Client side
        HANDLER.registerMessage(9,  PacketSyncConfig.class, PacketSyncConfig::encode, PacketSyncConfig::decode, PacketSyncConfig.Handler::handle);

        HANDLER.registerMessage(10, PacketTemplateManagerLoad.class, PacketTemplateManagerLoad::encode, PacketTemplateManagerLoad::decode, PacketTemplateManagerLoad.Handler::handle);
        HANDLER.registerMessage(11, PacketTemplateManagerPaste.class, PacketTemplateManagerPaste::encode, PacketTemplateManagerPaste::decode, PacketTemplateManagerPaste.Handler::handle);
        HANDLER.registerMessage(12, PacketTemplateManagerSave.class, PacketTemplateManagerSave::encode, PacketTemplateManagerSave::decode, PacketTemplateManagerSave.Handler::handle);
        HANDLER.registerMessage(13, PacketToggleMode.class, PacketToggleMode::encode, PacketToggleMode::decode, PacketToggleMode.Handler::handle);
        HANDLER.registerMessage(14, PacketUndoKey.class, PacketUndoKey::encode, PacketUndoKey::decode, PacketUndoKey.Handler::handle);
    }

    public static void sendTo(Object msg, EntityPlayerMP player)
    {
        if (!(player instanceof FakePlayer))
        {
            HANDLER.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendToServer(Object msg)
    {
        HANDLER.sendToServer(msg);
    }
}
