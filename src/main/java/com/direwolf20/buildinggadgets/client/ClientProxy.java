package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.renders.BuildingRender;
import com.direwolf20.buildinggadgets.client.renders.DestructionRender;
import com.direwolf20.buildinggadgets.client.renders.ExchangerRender;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.blocks.Models.BakedModelLoader;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.entities.*;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.proxy.CommonProxy;
import com.direwolf20.buildinggadgets.common.utils.PasteContainerMeshDefinition;
import com.direwolf20.buildinggadgets.common.utils.ToolRenders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import static com.direwolf20.buildinggadgets.common.items.ModItems.buildingGadget;

import java.awt.Color;

@Mod.EventBusSubscriber(value = Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        ModEntities.initModels();
        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
        super.preInit(e);
    }

    @Override
    public void init() {
        super.init();
        KeyBindings.init();
        ModBlocks.initColorHandlers();
    }

    @SubscribeEvent
    public static void registerModels(@SuppressWarnings("unused") ModelRegistryEvent event) {
        ModBlocks.effectBlock.initModel();
        ModBlocks.templateManager.initModel();
        buildingGadget.initModel();
        ModItems.exchangingGadget.initModel();
        ModItems.copyGadget.initModel();
        ModItems.template.initModel();
        if (SyncedConfig.enableDestructionGadget) {
            ModItems.destructionGadget.initModel();
        }
        if (SyncedConfig.enablePaste) {
            ModItems.constructionPaste.initModel();
            ModItems.constructionChunkDense.initModel();
            ModItems.constructionPasteContainer.initModel();
            ModItems.constructionPasteContainert2.initModel();
            ModItems.constructionPasteContainert3.initModel();
            ModItems.constructionPasteContainerCreative.initModel();
            ModBlocks.constructionBlockDense.initModel();
            ModBlocks.constructionBlock.initModel();
            ModBlocks.constructionBlockPowder.initModel();
            ModelLoader.setCustomMeshDefinition(ModItems.constructionPasteContainer, new PasteContainerMeshDefinition());
            ModelLoader.setCustomMeshDefinition(ModItems.constructionPasteContainert2, new PasteContainerMeshDefinition());
            ModelLoader.setCustomMeshDefinition(ModItems.constructionPasteContainert3, new PasteContainerMeshDefinition());
        }
    }

    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, new BlockBuildEntityRender.Factory());
        RenderingRegistry.registerEntityRenderingHandler(ConstructionBlockEntity.class, new ConstructionBlockEntityRender.Factory());
    }

    @SubscribeEvent
    public static void renderWorldLastEvent(RenderWorldLastEvent evt) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        AbstractGadget.getGadget(player).ifPresent(gadget -> {
            if (gadget.getItem() instanceof BuildingGadget) {
                new BuildingRender().render(evt, player, gadget);
            } else if (gadget.getItem() instanceof ExchangingGadget) {
                new ExchangerRender().render(evt, player, gadget);
            } else if (gadget.getItem() instanceof CopyGadget) {
                ToolRenders.renderPasteOverlay(evt, player, gadget);
            } else if (gadget.getItem() instanceof DestructionGadget) {
                new DestructionRender().render(evt, player, gadget);
            }
        });
    }

    @SubscribeEvent
    public static void registerSprites(TextureStitchEvent.Pre event) {
        registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TOOL);
        registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TEMPLATE);
    }

    private static void registerSprite(TextureStitchEvent.Pre event, String loc) {
        event.getMap().registerSprite(new ResourceLocation(loc));
    }

    public static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(sound, pitch));
    }

    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}