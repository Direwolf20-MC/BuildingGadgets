package com.direwolf20.buildinggadgets.common.events;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.InGameConfig;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketRequestConfigSync;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber
public class ConfigEventHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            BuildingGadgets.logger.info("Sending InGameConfig to freshly logged in client.");
            InGameConfig.sendConfigUpdateTo((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onConfigurationChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
        if (event.getModID().equals(BuildingGadgets.MODID)) {
            BuildingGadgets.logger.info("Configuration changed");
            ConfigManager.sync(BuildingGadgets.MODID, Type.INSTANCE);
            PacketHandler.INSTANCE.sendToServer(new PacketRequestConfigSync());
        }
    }
}
