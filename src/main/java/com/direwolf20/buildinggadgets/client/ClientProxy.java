package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.cache.CacheTemplateProvider;
import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.models.ConstructionBakedModel;
import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.containers.OurContainers;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ClientProxy {
    public static final CacheTemplateProvider CACHE_TEMPLATE_PROVIDER = new CacheTemplateProvider();
    public static void clientSetup(final IEventBus eventBus) {
        KeyBindings.init();

        eventBus.addListener(ClientProxy::bakeModels);
        eventBus.addListener(ClientProxy::registerSprites);
        MinecraftForge.EVENT_BUS.addListener(EventTooltip::onDrawTooltip);
        MinecraftForge.EVENT_BUS.addListener(ClientProxy::onPlayerLoggedOut);

        ScreenManager.registerFactory(OurContainers.TEMPLATE_MANAGER_CONTAINER.get(), TemplateManagerGUI::new);
        ((ConstructionBlock) OurBlocks.CONSTRUCTION_BLOCK.get()).initColorHandler(Minecraft.getInstance().getBlockColors());

        RenderTypeLookup.setRenderLayer(OurBlocks.CONSTRUCTION_BLOCK.get(), (RenderType) -> true);
        CACHE_TEMPLATE_PROVIDER.registerUpdateListener(((GadgetCopyPaste) OurItems.COPY_PASTE_GADGET_ITEM.get()).getRender());
    }

    private static void registerSprites(TextureStitchEvent.Pre event) {
        event.addSprite(new ResourceLocation(TemplateManagerContainer.TEXTURE_LOC_SLOT_TOOL));
        event.addSprite(new ResourceLocation(TemplateManagerContainer.TEXTURE_LOC_SLOT_TEMPLATE));
    }

    public static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(sound, pitch));
    }

    private static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        CACHE_TEMPLATE_PROVIDER.clear();
    }

    private static void bakeModels(ModelBakeEvent event) {
        ResourceLocation ConstrName = new ResourceLocation(Reference.MODID, "construction_block");
        TextureAtlasSprite breakPart = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState()).getParticleTexture();
        ModelResourceLocation ConstrLocation1 = new ModelResourceLocation(ConstrName, "ambient_occlusion=false,bright=false,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation1a = new ModelResourceLocation(ConstrName, "ambient_occlusion=true,bright=false,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation2 = new ModelResourceLocation(ConstrName, "ambient_occlusion=false,bright=true,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation2a = new ModelResourceLocation(ConstrName, "ambient_occlusion=true,bright=true,neighbor_brightness=false");
        ModelResourceLocation ConstrLocation3 = new ModelResourceLocation(ConstrName, "ambient_occlusion=false,bright=false,neighbor_brightness=true");
        ModelResourceLocation ConstrLocation3a = new ModelResourceLocation(ConstrName, "ambient_occlusion=true,bright=false,neighbor_brightness=true");
        ModelResourceLocation ConstrLocation4 = new ModelResourceLocation(ConstrName, "ambient_occlusion=false,bright=true,neighbor_brightness=true");
        ModelResourceLocation ConstrLocation4a = new ModelResourceLocation(ConstrName, "ambient_occlusion=true,bright=true,neighbor_brightness=true");
        IDynamicBakedModel constructionBakedModel = new ConstructionBakedModel();
        IDynamicBakedModel bakedModelLoader = new IDynamicBakedModel() {
            @Override
            public boolean isGui3d() {
                return false;
            }

            @Override
            public boolean isSideLit() { //isSideLit maybe?
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
                RenderType layer = MinecraftForgeClient.getRenderLayer();
                if (facadeState == null || facadeState == Blocks.AIR.getDefaultState())
                    facadeState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().getDefaultState();
                if (layer != null && ! RenderTypeLookup.canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
                    return Collections.emptyList();
                }
                model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(facadeState);
                return model.getQuads(facadeState, side, rand);

            }

            @Override
            public TextureAtlasSprite getParticleTexture() {
                //Fixes a crash until forge does something
                return breakPart;
            }

            @Override
            public ItemOverrideList getOverrides() {
                return null;
            }

            @Nonnull
            @Override
            public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
                return tileData;
            }
        };

        IDynamicBakedModel bakedModelLoaderAmbient = new IDynamicBakedModel() {
            @Override
            public boolean isGui3d() {
                return false;
            }

            @Override
            public boolean isSideLit() {
                return false;
            } // is side lit maybe?

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
                RenderType layer = MinecraftForgeClient.getRenderLayer();
                if (facadeState == null || facadeState == Blocks.AIR.getDefaultState())
                    facadeState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().getDefaultState();
                if (layer != null && ! RenderTypeLookup.canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
                    return Collections.emptyList();
                }
                model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(facadeState);
                return model.getQuads(facadeState, side, rand);

            }

            @Override
            public TextureAtlasSprite getParticleTexture() {
                //Fixes a crash until forge does something
                return breakPart;
            }

            @Override
            public ItemOverrideList getOverrides() {
                return null;
            }

            @Nonnull
            @Override
            public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
                return tileData;
            }
        };
        event.getModelRegistry().put(ConstrLocation1, bakedModelLoader);
        event.getModelRegistry().put(ConstrLocation2, bakedModelLoader);
        event.getModelRegistry().put(ConstrLocation3, bakedModelLoader);
        event.getModelRegistry().put(ConstrLocation4, bakedModelLoader);
        event.getModelRegistry().put(ConstrLocation1a, bakedModelLoaderAmbient);
        event.getModelRegistry().put(ConstrLocation2a, bakedModelLoaderAmbient);
        event.getModelRegistry().put(ConstrLocation3a, bakedModelLoaderAmbient);
        event.getModelRegistry().put(ConstrLocation4a, bakedModelLoaderAmbient);
    }
}
