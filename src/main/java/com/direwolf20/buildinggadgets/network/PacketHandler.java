package com.direwolf20.buildinggadgets.network;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(BuildingGadgets.MODID);

    public static void registerMessages() {

        // Server side
        INSTANCE.registerMessage(PacketToggleMode.Handler.class, PacketToggleMode.class, 0, Side.SERVER);
        INSTANCE.registerMessage(PacketChangeRange.Handler.class, PacketChangeRange.class, 1, Side.SERVER);
        INSTANCE.registerMessage(PacketUndoKey.Handler.class, PacketUndoKey.class, 2, Side.SERVER);
        INSTANCE.registerMessage(PacketAnchorKey.Handler.class, PacketAnchorKey.class, 3, Side.SERVER);
        INSTANCE.registerMessage(PacketBlockMap.Handler.class, PacketBlockMap.class, 4, Side.CLIENT);
        INSTANCE.registerMessage(PacketRequestBlockMap.Handler.class, PacketRequestBlockMap.class, 5, Side.SERVER);
        INSTANCE.registerMessage(PacketTemplateManagerSave.Handler.class, PacketTemplateManagerSave.class, 6, Side.SERVER);
        INSTANCE.registerMessage(PacketTemplateManagerLoad.Handler.class, PacketTemplateManagerLoad.class, 7, Side.SERVER);
        INSTANCE.registerMessage(PacketTemplateBlockMap.Handler.class, PacketTemplateBlockMap.class, 8, Side.CLIENT);
        INSTANCE.registerMessage(PacketRequestTemplateBlockMap.Handler.class, PacketRequestTemplateBlockMap.class, 9, Side.SERVER);
        INSTANCE.registerMessage(PacketTemplateManagerPaste.Handler.class, PacketTemplateManagerPaste.class, 11, Side.SERVER);
        // Client side
    }
}
