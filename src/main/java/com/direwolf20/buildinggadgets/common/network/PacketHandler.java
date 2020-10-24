package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.network.packets.*;
import com.direwolf20.buildinggadgets.common.network.split.PacketSplitManager;
import com.direwolf20.buildinggadgets.common.network.split.SplitPacket;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(4);
    private static final PacketSplitManager SPLIT_MANAGER = new PacketSplitManager();

    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            Reference.NETWORK_CHANNEL_ID_MAIN,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static PacketSplitManager getSplitManager() {
        return SPLIT_MANAGER;
    }

    public static void register() {
        int index = 0;

        // Server side
        HANDLER.registerMessage(index++, PacketAnchor.class, PacketAnchor::encode, PacketAnchor::decode, PacketAnchor.Handler::handle);
        HANDLER.registerMessage(index++, PacketBindTool.class, PacketBindTool::encode, PacketBindTool::decode, PacketBindTool.Handler::handle);
        HANDLER.registerMessage(index++, PacketToggleFuzzy.class, PacketToggleFuzzy::encode, PacketToggleFuzzy::decode, PacketToggleFuzzy.Handler::handle);
        HANDLER.registerMessage(index++, PacketToggleConnectedArea.class, PacketToggleConnectedArea::encode, PacketToggleConnectedArea::decode, PacketToggleConnectedArea.Handler::handle);
        HANDLER.registerMessage(index++, PacketToggleRayTraceFluid.class, PacketToggleRayTraceFluid::encode, PacketToggleRayTraceFluid::decode, PacketToggleRayTraceFluid.Handler::handle);
        HANDLER.registerMessage(index++, PacketToggleBlockPlacement.class, PacketToggleBlockPlacement::encode, PacketToggleBlockPlacement::decode, PacketToggleBlockPlacement.Handler::handle);
        HANDLER.registerMessage(index++, PacketChangeRange.class, PacketChangeRange::encode, PacketChangeRange::decode, PacketChangeRange.Handler::handle);
        HANDLER.registerMessage(index++, PacketRotateMirror.class, PacketRotateMirror::encode, PacketRotateMirror::decode, PacketRotateMirror.Handler::handle);
        HANDLER.registerMessage(index++, PacketCopyCoords.class, PacketCopyCoords::encode, PacketCopyCoords::decode, PacketCopyCoords.Handler::handle);
        HANDLER.registerMessage(index++, PacketDestructionGUI.class, PacketDestructionGUI::encode, PacketDestructionGUI::decode, PacketDestructionGUI.Handler::handle);
        HANDLER.registerMessage(index++, PacketPasteGUI.class, PacketPasteGUI::encode, PacketPasteGUI::decode, PacketPasteGUI.Handler::handle);
        HANDLER.registerMessage(index++, PacketToggleMode.class, PacketToggleMode::encode, PacketToggleMode::decode, PacketToggleMode.Handler::handle);
        HANDLER.registerMessage(index++, PacketUndo.class, PacketUndo::encode, PacketUndo::decode, PacketUndo.Handler::handle);

        // Both Sides
        HANDLER.registerMessage(index++, SplitPacket.class, SPLIT_MANAGER::encode, SPLIT_MANAGER::decode, SPLIT_MANAGER::handle);
        getSplitManager().registerSplitPacket(SplitPacketUpdateTemplate.class, SplitPacketUpdateTemplate::encode, SplitPacketUpdateTemplate::new, SplitPacketUpdateTemplate::handle);
        HANDLER.registerMessage(index++, PacketSetRemoteInventoryCache.class, PacketSetRemoteInventoryCache::encode, PacketSetRemoteInventoryCache::decode, PacketSetRemoteInventoryCache.Handler::handle);
        HANDLER.registerMessage(index++, PacketRequestTemplate.class, PacketRequestTemplate::encode, PacketRequestTemplate::new, PacketRequestTemplate::handle);
        //Client side
        HANDLER.registerMessage(index++, PacketTemplateManagerTemplateCreated.class, PacketTemplateManagerTemplateCreated::encode, PacketTemplateManagerTemplateCreated::new, PacketTemplateManagerTemplateCreated::handle);
    }

    public static void sendTo(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer))
            HANDLER.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }

    public static void send(Object msg, PacketTarget target) {
        HANDLER.send(target, msg);
    }
}
