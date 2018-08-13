package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.blocks.Models.BakedModelLoader;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntityRender;
import com.direwolf20.buildinggadgets.entities.ConstructionBlockEntity;
import com.direwolf20.buildinggadgets.entities.ConstructionBlockEntityRender;
import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import com.direwolf20.buildinggadgets.items.GenericGadget;
import com.direwolf20.buildinggadgets.tools.PasteContainerMeshDefinition;
import com.direwolf20.buildinggadgets.tools.ToolRenders;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import static com.direwolf20.buildinggadgets.ModItems.buildingTool;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = BuildingGadgets.MODID)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        ModEntities.initModels();
        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
        super.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        KeyBindings.init();
        ModBlocks.initColorHandlers();
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.effectBlock.initModel();
        buildingTool.initModel();
        ModItems.exchangerTool.initModel();
        ModItems.copyPasteTool.initModel();
        if (Config.enablePaste) {
            ModItems.constructionPaste.initModel();
            ModItems.constructionPasteContainer.initModel();
            ModBlocks.constructionBlock.initModel();
            ModBlocks.constructionBlockPowder.initModel();
            ModelLoader.setCustomMeshDefinition(ModItems.constructionPasteContainer, new PasteContainerMeshDefinition());
        }
    }

    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, new BlockBuildEntityRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(ConstructionBlockEntity.class, new ConstructionBlockEntityRender.Factory());
    }

    @SubscribeEvent
    public static void renderWorldLastEvent(RenderWorldLastEvent evt) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer p = mc.player;
        ItemStack heldItem = p.getHeldItemMainhand();
        if (!(heldItem.getItem() instanceof GenericGadget)) {
            heldItem = p.getHeldItemOffhand();
            if (!(heldItem.getItem() instanceof GenericGadget)) {
                return;
            }
        }
        if (heldItem.getItem() instanceof BuildingTool) {
            BuildingTool buildingTool = (BuildingTool) heldItem.getItem();
            ToolRenders.renderBuilderOverlay(evt, p, heldItem);
        } else if (heldItem.getItem() instanceof ExchangerTool) {
            ExchangerTool exchangerTool = (ExchangerTool) heldItem.getItem();
            ToolRenders.renderExchangerOverlay(evt, p, heldItem);
        } else if (heldItem.getItem() instanceof CopyPasteTool) {
            //CopyPasteTool copyPasteTool = (CopyPasteTool) heldItem.getItem();
            ToolRenders.renderPasteOverlay(evt, p, heldItem);
        }

    }
}