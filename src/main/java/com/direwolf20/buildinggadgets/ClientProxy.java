package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.Entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.Entities.BlockBuildEntityRender;
import net.minecraftforge.client.event.ModelRegistryEvent;
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
        //MinecraftForge.EVENT_BUS.register(new TextureStitcher());
        ModEntities.initModels();
        super.preInit(e);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        //ModBlocks.gooBlock.initModel();
        //ModBlocks.turretLaser.initModel();
        ModItems.buildingTool.initModel();
        ModItems.exchangerTool.initModel();
    }

    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, new BlockBuildEntityRender.Factory());
        //RenderingRegistry.registerEntityRenderingHandler(LaserBlastEntity.class, new LaserBlastEntityRender.Factory());
    }
}