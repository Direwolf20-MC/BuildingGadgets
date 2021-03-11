package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.network.packets.*;
import com.direwolf20.buildinggadgets.common.network.split.PacketSplitManager;
import com.direwolf20.buildinggadgets.common.network.split.SplitPacket;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(4);
    private static short index = 0;
    private static final PacketSplitManager SPLIT_MANAGER = new PacketSplitManager();

    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Reference.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static PacketSplitManager getSplitManager() {
        return SPLIT_MANAGER;
    }

    public static void register() {
        // Server side
        registerMessage(PacketAnchor.class, PacketAnchor::encode, PacketAnchor::decode, PacketAnchor.Handler::handle);
        registerMessage(PacketBindTool.class, PacketBindTool::encode, PacketBindTool::decode, PacketBindTool.Handler::handle);
        registerMessage(PacketToggleFuzzy.class, PacketToggleFuzzy::encode, PacketToggleFuzzy::decode, PacketToggleFuzzy.Handler::handle);
        registerMessage(PacketToggleFluidOnly.class, PacketToggleFluidOnly::encode, PacketToggleFluidOnly::decode, PacketToggleFluidOnly.Handler::handle);
        registerMessage(PacketToggleConnectedArea.class, PacketToggleConnectedArea::encode, PacketToggleConnectedArea::decode, PacketToggleConnectedArea.Handler::handle);
        registerMessage(PacketToggleRayTraceFluid.class, PacketToggleRayTraceFluid::encode, PacketToggleRayTraceFluid::decode, PacketToggleRayTraceFluid.Handler::handle);
        registerMessage(PacketToggleBlockPlacement.class, PacketToggleBlockPlacement::encode, PacketToggleBlockPlacement::decode, PacketToggleBlockPlacement.Handler::handle);
        registerMessage(PacketChangeRange.class, PacketChangeRange::encode, PacketChangeRange::decode, PacketChangeRange.Handler::handle);
        registerMessage(PacketRotateMirror.class, PacketRotateMirror::encode, PacketRotateMirror::decode, PacketRotateMirror.Handler::handle);
        registerMessage(PacketCopyCoords.class, PacketCopyCoords::encode, PacketCopyCoords::decode, PacketCopyCoords.Handler::handle);
        registerMessage(PacketDestructionGUI.class, PacketDestructionGUI::encode, PacketDestructionGUI::decode, PacketDestructionGUI.Handler::handle);
        registerMessage(PacketPasteGUI.class, PacketPasteGUI::encode, PacketPasteGUI::decode, PacketPasteGUI.Handler::handle);
        registerMessage(PacketToggleMode.class, PacketToggleMode::encode, PacketToggleMode::decode, PacketToggleMode.Handler::handle);
        registerMessage(PacketUndo.class, PacketUndo::encode, PacketUndo::decode, PacketUndo.Handler::handle);

        // Both Sides
        registerMessage(SplitPacket.class, SPLIT_MANAGER::encode, SPLIT_MANAGER::decode, SPLIT_MANAGER::handle);
        getSplitManager().registerSplitPacket(SplitPacketUpdateTemplate.class, SplitPacketUpdateTemplate::encode, SplitPacketUpdateTemplate::new, SplitPacketUpdateTemplate::handle);

        registerMessage(PacketSetRemoteInventoryCache.class, PacketSetRemoteInventoryCache::encode, PacketSetRemoteInventoryCache::decode, PacketSetRemoteInventoryCache.Handler::handle);
        registerMessage(PacketRequestTemplate.class, PacketRequestTemplate::encode, PacketRequestTemplate::new, PacketRequestTemplate::handle);
        //Client side
        registerMessage(PacketTemplateManagerTemplateCreated.class, PacketTemplateManagerTemplateCreated::encode, PacketTemplateManagerTemplateCreated::new, PacketTemplateManagerTemplateCreated::handle);
    }

    public static void sendTo(Object msg, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer))
            HANDLER.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }

    public static void send(Object msg, PacketTarget target) {
        HANDLER.send(target, msg);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<Context>> messageConsumer) {
        HANDLER.registerMessage(index, messageType, encoder, decoder, messageConsumer);
        index++;
        if (index > 0xFF)
            throw new RuntimeException("Too many messages!");
    }
}
