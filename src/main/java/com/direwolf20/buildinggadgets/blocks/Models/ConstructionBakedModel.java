package com.direwolf20.buildinggadgets.blocks.Models;

import com.direwolf20.buildinggadgets.BuildingGadgets;
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
    private IBakedModel blankConstructionModel;

    public ConstructionBakedModel(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, IBakedModel blankConstrModel) {
        this.format = format;
        this.blankConstructionModel = blankConstrModel;
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
        IBakedModel model;
        if (facadeId == null) {
            return blankConstructionModel.getQuads(state, side, rand);
        }

        IBlockState facadeState = facadeId.getBlockState();
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

        if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
            return Collections.emptyList();
        }
        model = getModel(facadeState);
        try {
            return model.getQuads(state, side, rand);
        } catch (Exception e) {
            return model.getQuads(facadeState, side, rand);
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

    /*@Override //This is causing darkness on stairs, and I have no idea why.
    public boolean isAmbientOcclusion(IBlockState state) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        ConstructionID facadeId = extendedBlockState.getValue(ConstructionBlock.FACADEID);
        if (facadeId == null) {
            return true;
        }
        IBlockState facadeState = facadeId.getBlockState();
        return facadeState.getBlock().isOpaqueCube(facadeState);
    }*/

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
        return blankConstructionModel.getParticleTexture();
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
