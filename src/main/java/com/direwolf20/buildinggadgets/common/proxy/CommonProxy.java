package com.direwolf20.buildinggadgets.common.proxy;


import com.direwolf20.buildinggadgets.client.gui.GuiProxy;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.*;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManager;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.entities.ModEntities;
import com.direwolf20.buildinggadgets.common.items.*;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;


@Mod.EventBusSubscriber(modid = BuildingGadgets.MODID)
public class CommonProxy {
    private boolean applyCompatConfig = false;
//    REIMPLEMENT
//    public void preInit(FMLPreInitializationEvent e) {
//        ModEntities.init();
//        PacketHandler.registerMessages();
//    }

    public void init() {
//        REIMPLEMENT
//        NetworkRegistry.INSTANCE.registerGuiHandler(BuildingGadgets.instance, new GuiProxy());
    }

    public void postInit() { }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new EffectBlock());
        event.getRegistry().register(new TemplateManager());
//        REIMPLEMENT
//        GameRegistry.registerTileEntity(TemplateManagerTileEntity.class, new ResourceLocation(BuildingGadgets.MODID, "templateManager"));
        if (SyncedConfig.enablePaste) {
            event.getRegistry().register(new ConstructionBlock());
            event.getRegistry().register(new ConstructionBlockPowder());
//        REIMPLEMENT
//            GameRegistry.registerTileEntity(ConstructionBlockTileEntity.class, new ResourceLocation(BuildingGadgets.MODID,  "_constructionBlock"));
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new GadgetBuilding());
        event.getRegistry().register(new GadgetExchanger());
        event.getRegistry().register(new GadgetCopyPaste());
//        REIMPLEMENT
//        event.getRegistry().register(new ItemBlock(ModBlocks.templateManager).setRegistryName(ModBlocks.templateManager.getRegistryName()));
        event.getRegistry().register(new Template());
        if (SyncedConfig.enableDestructionGadget) {
            event.getRegistry().register(new GadgetDestruction());
        }
        if (SyncedConfig.enablePaste) {
//        REIMPLEMENT
//            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlock).setRegistryName(ModBlocks.constructionBlock.getRegistryName()));
//            event.getRegistry().register(new ItemBlock(ModBlocks.constructionBlockPowder).setRegistryName(ModBlocks.constructionBlockPowder.getRegistryName()));
            event.getRegistry().register(new ConstructionPaste());
            event.getRegistry().register(new ConstructionPasteContainer());
            event.getRegistry().register(new ConstructionPasteContainerT2());
            event.getRegistry().register(new ConstructionPasteContainerT3());
        }
    }
}