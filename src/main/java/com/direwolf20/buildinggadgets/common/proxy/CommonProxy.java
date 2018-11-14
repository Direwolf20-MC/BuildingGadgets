package com.direwolf20.buildinggadgets.common.proxy;


import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.Config;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.entities.ModEntities;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockPowder;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.client.gui.GuiProxy;
import com.direwolf20.buildinggadgets.common.items.*;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
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

    public void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(BuildingGadgets.instance, new GuiProxy());
    }

    public void postInit() {
        if (config.hasChanged()) {
            config.save();
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new EffectBlock());
        event.getRegistry().register(new TemplateManager());
        GameRegistry.registerTileEntity(TemplateManagerTileEntity.class, BuildingGadgets.MODID + ":templateManager");
        if (Config.enablePaste) {
            event.getRegistry().register(new ConstructionBlock());
            event.getRegistry().register(new ConstructionBlockPowder());
            GameRegistry.registerTileEntity(ConstructionBlockTileEntity.class, BuildingGadgets.MODID + "_constructionBlock");
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new GadgetBuilding());
        event.getRegistry().register(new GadgetExchanger());
        event.getRegistry().register(new GadgetCopyPaste());
        event.getRegistry().register(new ItemBlock(ModBlocks.templateManager).setRegistryName(ModBlocks.templateManager.getRegistryName()));
        event.getRegistry().register(new Template());
        if (Config.enableDestructionTool) {
            event.getRegistry().register(new GadgetDestruction());
        }
        if (Config.enablePaste) {
            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlock).setRegistryName(ModBlocks.constructionBlock.getRegistryName()));
            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlockPowder).setRegistryName(ModBlocks.constructionBlockPowder.getRegistryName()));
            event.getRegistry().register(new ConstructionPaste());
            event.getRegistry().register(new ConstructionPasteContainer());
            event.getRegistry().register(new ConstructionPasteContainerT2());
            event.getRegistry().register(new ConstructionPasteContainerT3());
        }
    }
}