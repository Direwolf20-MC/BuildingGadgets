package com.direwolf20.buildinggadgets.client;

import afu.org.checkerframework.checker.nullness.qual.Nullable;
import com.direwolf20.buildinggadgets.client.events.EventClientTick;
import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.blocks.templatemanager.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGContainers;
import com.direwolf20.buildinggadgets.common.registry.objects.BuildingObjects;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(Dist.CLIENT)
public class ClientProxy {

    public static void clientSetup(final IEventBus eventBus) {
        DeferredWorkQueue.runLater(KeyBindings::init);
        MinecraftForge.EVENT_BUS.addListener(BuildingObjects::initColorHandlers);
        //eventBus.addListener(ClientProxy::renderWorldLastEvent);
        MinecraftForge.EVENT_BUS.addListener(ClientProxy::bakeModels);
        MinecraftForge.EVENT_BUS.addListener(EventClientTick::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(EventTooltip::onDrawTooltip);
        ScreenManager.registerFactory(BGContainers.TEMPLATE_MANAGER_CONTAINER, (ScreenManager.IScreenFactory<TemplateManagerContainer, TemplateManagerGUI>) TemplateManagerGUI::new);
    }

    private static void bakeModels(ModelBakeEvent event) {
        ResourceLocation ConstrName = new ResourceLocation(Reference.MODID, "construction_block");
        ModelResourceLocation ConstrLocation1 = new ModelResourceLocation(ConstrName, "bright=false,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation2 = new ModelResourceLocation(ConstrName, "bright=true,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation3 = new ModelResourceLocation(ConstrName, "bright=false,neighbor_brightness=true");
        ModelResourceLocation ConstrLocation4 = new ModelResourceLocation(ConstrName, "bright=true,neighbor_brightness=true");
        IDynamicBakedModel bakedModelLoader = new IDynamicBakedModel() {
            @Override
            public boolean isGui3d() {
                return false;
            }

            @Override
            public boolean isBuiltInRenderer() {
                return false;
            }

            @Override
            public boolean isAmbientOcclusion() {
                return true;
            }

            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData) {
                IBakedModel model;
                BlockState facadeState = modelData.getData(ConstructionBlockTileEntity.FACADE_STATE);
                BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
                if (facadeState == null || facadeState == Blocks.AIR.getDefaultState())
                    facadeState = BGBlocks.constructionBlockDense.getDefaultState();
                if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
                    return Collections.emptyList();
                }
                model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(facadeState);
                return model.getQuads(facadeState, side, rand);

            }

            @Override
            public TextureAtlasSprite getParticleTexture() {
                return MissingTextureSprite.func_217790_a();
            }

            @Override
            public ItemOverrideList getOverrides() {
                return null;
            }

            @Override
            @Nonnull
            public IModelData getModelData(@Nonnull IEnviromentBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
                return tileData;
            }
        };
        event.getModelRegistry().put(ConstrLocation1, bakedModelLoader);
        event.getModelRegistry().put(ConstrLocation2, bakedModelLoader);
        event.getModelRegistry().put(ConstrLocation3, bakedModelLoader);
        event.getModelRegistry().put(ConstrLocation4, bakedModelLoader);
    }

    @SubscribeEvent
    public static void registerSprites(TextureStitchEvent.Pre event) {
        //registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TOOL);
        //registerSprite(event, TemplateManagerContainer.TEXTURE_LOC_SLOT_TEMPLATE);
    }

    private static void registerSprite(TextureStitchEvent.Pre event, String loc) {
        //TODO replace with something that doesn't result in an StackOverflow error
        //event.getMap().func_215254_a(Minecraft.getInstance().getResourceManager(), Collections.singleton(new ResourceLocation(loc)), null);
    }

    public static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(sound, pitch));
    }
}
