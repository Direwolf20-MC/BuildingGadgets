package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(BuildingGadgets.MODID);
    private static int packetId = 0;

    public static enum Side {
        CLIENT, SERVER, BOTH;
    }

    public static void registerMessages() {

        // Server side
        registerMessage(PacketToggleMode.Handler.class, PacketToggleMode.class, Side.SERVER);
        registerMessage(PacketChangeRange.Handler.class, PacketChangeRange.class, Side.SERVER);
        registerMessage(PacketUndoKey.Handler.class, PacketUndoKey.class, Side.SERVER);
        registerMessage(PacketAnchorKey.Handler.class, PacketAnchorKey.class, Side.SERVER);
        registerMessage(PacketRequestBlockMap.Handler.class, PacketRequestBlockMap.class, Side.SERVER);
        registerMessage(PacketTemplateManagerSave.Handler.class, PacketTemplateManagerSave.class, Side.SERVER);
        registerMessage(PacketTemplateManagerLoad.Handler.class, PacketTemplateManagerLoad.class, Side.SERVER);
        registerMessage(PacketTemplateManagerPaste.Handler.class, PacketTemplateManagerPaste.class, Side.SERVER);
        registerMessage(PacketCopyCoords.Handler.class, PacketCopyCoords.class, Side.SERVER);
        registerMessage(PacketDestructionGUI.Handler.class, PacketDestructionGUI.class, Side.SERVER);
        registerMessage(PacketPasteGUI.Handler.class, PacketPasteGUI.class, Side.SERVER);
        registerMessage(PacketRequestConfigSync.Handler.class, PacketRequestConfigSync.class, Side.SERVER);
        // Client side
        registerMessage(PacketSyncConfig.Handler.class,PacketSyncConfig.class,Side.CLIENT);
        registerMessage(PacketBlockMap.Handler.class, PacketBlockMap.class, Side.CLIENT);

        // Both sides
        registerMessage(PacketSetRemoteInventoryCache.Handler.class, PacketSetRemoteInventoryCache.class, Side.BOTH);
    }

    private static void registerMessage(Class handler, Class packet, Side side) {
        if (side != Side.CLIENT)
            registerMessage(handler, packet, net.minecraftforge.fml.relauncher.Side.SERVER);
        
        if (side != Side.SERVER)
            registerMessage(handler, packet, net.minecraftforge.fml.relauncher.Side.CLIENT);
    }

    private static void registerMessage(Class handler, Class packet, net.minecraftforge.fml.relauncher.Side side) {
        INSTANCE.registerMessage(handler, packet, packetId++, side);
    }
}
