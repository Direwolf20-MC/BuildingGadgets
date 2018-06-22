package com.direwolf20.buildinggadgets;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        // register texture stitcher
        ModEntities.initModels();
        //MinecraftForge.EVENT_BUS.register(new TextureStitcher());
        super.preInit(e);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        //ModBlocks.gooBlock.initModel();
        //ModBlocks.turretLaser.initModel();
        //ModItems.gooDust.initModel();
        //ModItems.laserGun.initModel();
    }

    public void registerEntityRenderers() {
        //RenderingRegistry.registerEntityRenderingHandler(LaserGunEntity.class, new LaserGunEntityRender.Factory());
        //RenderingRegistry.registerEntityRenderingHandler(LaserBlastEntity.class, new LaserBlastEntityRender.Factory());
    }
}