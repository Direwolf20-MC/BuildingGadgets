package com.direwolf20.buildinggadgets.common;

import afu.org.checkerframework.checker.nullness.qual.Nullable;
import com.direwolf20.buildinggadgets.api.APIProxy;
import com.direwolf20.buildinggadgets.client.ClientProxy;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.commands.BlockMapCommand;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.config.crafting.CraftingConditionDestruction;
import com.direwolf20.buildinggadgets.common.config.crafting.CraftingConditionPaste;
import com.direwolf20.buildinggadgets.common.config.crafting.RecipeConstructionPaste;
import com.direwolf20.buildinggadgets.common.events.AnvilRepairHandler;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.registry.objects.BuildingObjects;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.command.Commands;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Mod(value = Reference.MODID)
public class BuildingGadgets {

    public static Logger LOG = LogManager.getLogger();
    private static BuildingGadgets theMod = null;
    private final APIProxy theApi;

    public static BuildingGadgets getInstance() {
        assert theMod != null;
        return theMod;
    }

    public BuildingGadgets() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(Type.COMMON, Config.COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_CONFIG);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::serverLoad);
        eventBus.addListener(this::finishLoad);
        MinecraftForge.EVENT_BUS.addListener(this::modelBake);
        eventBus.addGenericListener(IRecipeSerializer.class, this::onRecipeRegister);

        eventBus.addListener(Config::onLoad);
        eventBus.addListener(Config::onFileChange);

        MinecraftForge.EVENT_BUS.register(new AnvilRepairHandler());
        MinecraftForge.EVENT_BUS.register(this);


        // Client only registering
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            eventBus.addListener((Consumer<FMLClientSetupEvent>) event -> ClientProxy.clientSetup(eventBus));
            ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiMod::openScreen);
        });

        theApi = APIProxy.INSTANCE.onCreate(eventBus, MinecraftForge.EVENT_BUS, Config.API);
        BuildingObjects.init();
    }

    public void modelBake(ModelBakeEvent event)
    {
        final IBakedModel old = event.getModelRegistry().get(new ModelResourceLocation(Reference.BlockReference.CONSTRUCTION_BLOCK));
        event.getModelRegistry().put(new ModelResourceLocation(Reference.MODID,"construction_block"), new IDynamicBakedModel()
        {
            @Override
            public boolean isGui3d()
            {
                return false;
            }

            @Override
            public boolean isBuiltInRenderer()
            {
                return false;
            }

            @Override
            public boolean isAmbientOcclusion()
            {
                return true;
            }

            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData)
            {
                IBakedModel model;
                BlockState facadeState = modelData.getData(ConstructionBlockTileEntity.Facade_State);
                model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(facadeState);
                return model.getQuads(facadeState, side, rand);

            }

            @Override
            public TextureAtlasSprite getParticleTexture()
            {
                return MissingTextureSprite.func_217790_a();
            }

            @Override
            public ItemOverrideList getOverrides()
            {
                return null;
            }

            @Override
            @Nonnull
            public IModelData getModelData(@Nonnull IEnviromentBlockReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
            {
                return tileData;
            }
        });
    }

    private void setup(final FMLCommonSetupEvent event) {
        theMod = (BuildingGadgets) ModLoadingContext.get().getActiveContainer().getMod();
        theApi.onSetup();
        DeferredWorkQueue.runLater(() -> {
            PacketHandler.register();
            CraftingHelper.register(Reference.CONDITION_PASTE_ID, new CraftingConditionPaste());
            CraftingHelper.register(Reference.CONDITION_DESTRUCTION_ID, new CraftingConditionDestruction());
        });
        event.getIMCStream().forEach(APIProxy.INSTANCE::handleIMC);
    }

    private void serverLoad(FMLServerStartingEvent event) {
        event.getCommandDispatcher().register(
                Commands.literal(Reference.MODID)
                    .then(BlockMapCommand.registerList())
                    .then(BlockMapCommand.registerDelete())
        );
    }

    private void finishLoad(FMLLoadCompleteEvent event) {
        theApi.onLoadComplete();
        BuildingObjects.cleanup();
    }

    private void onRecipeRegister(final RegistryEvent.Register<IRecipeSerializer<?>> e) {
        e.getRegistry().register(
            new RecipeConstructionPaste.Serializer().setRegistryName(
                    new ResourceLocation(Reference.MODID, "construction_paste")
            )
        );
    }

}
