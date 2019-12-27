package com.direwolf20.buildinggadgets.common.proxy;


import com.direwolf20.buildinggadgets.client.gui.GuiProxy;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.ModSounds;
import com.direwolf20.buildinggadgets.common.blocks.*;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.config.CompatConfig;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.entities.ModEntities;
import com.direwolf20.buildinggadgets.common.gadgets.BuildingGadget;
import com.direwolf20.buildinggadgets.common.gadgets.DestructionGadget;
import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.gadgets.CopyGadget;
import com.direwolf20.buildinggadgets.common.gadgets.ExchangingGadget;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionChunkDense;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;
import com.direwolf20.buildinggadgets.common.items.pastes.RegularPasteContainerTypes;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;


@Mod.EventBusSubscriber
public class CommonProxy {
    private boolean applyCompatConfig = false;

    public void preInit(FMLPreInitializationEvent e) {
        ModEntities.init();
        PacketHandler.registerMessages();
        File cfgFile = new File(e.getModConfigurationDirectory(), "BuildingGadgets.cfg");
        if (cfgFile.exists()) {
            BuildingGadgets.logger.info("Preparing to migrate old config Data to new Format");
            applyCompatConfig = CompatConfig.readConfig(cfgFile);
        }
        IntegrationHandler.preInit(e);
    }

    public void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(BuildingGadgets.instance, new GuiProxy());
        if (applyCompatConfig) {
            BuildingGadgets.logger.info("Migrating old config Data.");
            CompatConfig.applyCompatConfig();
        }
        IntegrationHandler.init();
    }

    public void postInit() {
        IntegrationHandler.postInit();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new EffectBlock());
        event.getRegistry().register(new TemplateManager());
        GameRegistry.registerTileEntity(TemplateManagerTileEntity.class, new ResourceLocation(BuildingGadgets.MODID, "templateManager"));
        if (SyncedConfig.enablePaste) {
            event.getRegistry().register(new ConstructionBlockDense());
            event.getRegistry().register(new ConstructionBlock());
            event.getRegistry().register(new ConstructionBlockPowder());
            GameRegistry.registerTileEntity(ConstructionBlockTileEntity.class, new ResourceLocation(BuildingGadgets.MODID, "_constructionBlock"));
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new BuildingGadget());
        event.getRegistry().register(new ExchangingGadget());
        event.getRegistry().register(new CopyGadget());
        event.getRegistry().register(new ItemBlock(ModBlocks.templateManager).setRegistryName(ModBlocks.templateManager.getRegistryName()));
        event.getRegistry().register(new Template());
        if (SyncedConfig.enableDestructionGadget) {
            event.getRegistry().register(new DestructionGadget());
        }
        if (SyncedConfig.enablePaste) {
            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlockDense).setRegistryName(ModBlocks.constructionBlockDense.getRegistryName()));
            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlock).setRegistryName(ModBlocks.constructionBlock.getRegistryName()));
            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlockPowder).setRegistryName(ModBlocks.constructionBlockPowder.getRegistryName()));
            event.getRegistry().register(new ConstructionPaste());
            event.getRegistry().register(new ConstructionChunkDense());
            for (RegularPasteContainerTypes type : RegularPasteContainerTypes.values()) {
                event.getRegistry().register(new ConstructionPasteContainer(type.itemSuffix, type.capacitySupplier));
            }
            event.getRegistry().register(new ConstructionPasteContainerCreative());
        }
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        for (ModSounds sound : ModSounds.values()) {
            event.getRegistry().register(sound.getSound());
        }
    }
}