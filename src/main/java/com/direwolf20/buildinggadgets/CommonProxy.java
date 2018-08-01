package com.direwolf20.buildinggadgets;


import com.direwolf20.buildinggadgets.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.config.InGameConfig;
import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


@Mod.EventBusSubscriber(modid = BuildingGadgets.MODID)
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
        ModEntities.init();
        PacketHandler.registerMessages();
    }

    public void init(FMLInitializationEvent e) {

    }

    public void postInit(FMLPostInitializationEvent e) {

    }

    public void onServerStart(FMLServerStartingEvent e) {
        InGameConfig.init(); //In Singleplayer this will reset the Config (which might have been overridden by an remote server) to the local values
        //on an physical Server this will simply ensure that the class is loaded properly
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new EffectBlock());
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        //event.getRegistry().register(new ItemBlock(ModBlocks.effectBlock).setRegistryName(ModBlocks.effectBlock.getRegistryName()));
        event.getRegistry().register(new BuildingTool());
        event.getRegistry().register(new ExchangerTool());
    }
}