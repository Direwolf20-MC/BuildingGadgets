package com.direwolf20.buildinggadgets;


import com.direwolf20.buildinggadgets.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.ConstructionPaste;
import com.direwolf20.buildinggadgets.items.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;


@Mod.EventBusSubscriber(modid = BuildingGadgets.MODID)
public class CommonProxy {
    public static Configuration config;

    public void preInit(FMLPreInitializationEvent e) {
        ModEntities.init();
        PacketHandler.registerMessages();
        File directory = e.getModConfigurationDirectory();
        config = new Configuration(new File(directory.getPath(), "BuildingGadgets.cfg"));
        Config.readConfig();
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
        if (config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new EffectBlock());
        if (Config.enablePaste) {
            event.getRegistry().register(new ConstructionBlock());
            event.getRegistry().register(new ConstructionBlockPowder());
            GameRegistry.registerTileEntity(ConstructionBlockTileEntity.class, BuildingGadgets.MODID + "_constructionBlock");
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new BuildingTool());
        event.getRegistry().register(new ExchangerTool());
        if (Config.enablePaste) {
            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlock).setRegistryName(ModBlocks.constructionBlock.getRegistryName()));
            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlockPowder).setRegistryName(ModBlocks.constructionBlockPowder.getRegistryName()));
            event.getRegistry().register(new ConstructionPaste());
            event.getRegistry().register(new ConstructionPasteContainer());
        }
    }
}