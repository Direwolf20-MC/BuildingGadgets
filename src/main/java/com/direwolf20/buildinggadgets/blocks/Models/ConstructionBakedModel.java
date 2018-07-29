package com.direwolf20.buildinggadgets.blocks.Models;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.blocks.ConstructionBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ConstructionBakedModel implements IBakedModel {
    public static final ModelResourceLocation modelFacade = new ModelResourceLocation(BuildingGadgets.MODID + ":" + "constructionblock");

    private VertexFormat format;
    private static TextureAtlasSprite spriteCable;

    public ConstructionBakedModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.format = format;
    }

    private static void initTextures() {
        if (spriteCable == null) {
            spriteCable = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(BuildingGadgets.MODID + ":blocks/constructionblock");
        }
    }



    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        ConstructionID facadeId = extendedBlockState.getValue(ConstructionBlock.FACADEID);
        if (facadeId == null) {
            return Collections.emptyList();
        }

        IBlockState facadeState = facadeId.getBlockState();
        if (facadeState == ModBlocks.constructionBlock.getDefaultState()) {
            return Collections.emptyList();
        }
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
            return Collections.emptyList();
        }
        IBakedModel model = getModel(facadeState);
        try {
            return model.getQuads(state, side, rand);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private IBakedModel getModel(@Nonnull IBlockState state) {
        initTextures();
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
        return model;
    }


    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return spriteCable;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
