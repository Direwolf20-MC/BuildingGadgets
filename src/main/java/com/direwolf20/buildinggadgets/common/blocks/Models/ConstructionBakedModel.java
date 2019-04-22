package com.direwolf20.buildinggadgets.common.blocks.Models;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ConstructionBakedModel implements IBakedModel {
    public static final ModelResourceLocation modelFacade = new ModelResourceLocation(Reference.MODID + ":" + "construction_block");

    private static TextureAtlasSprite spriteCable;
    private IBakedModel blankConstructionModel;

    ConstructionBakedModel(IBakedModel blankConstrModel) {
        this.blankConstructionModel = blankConstrModel;
    }

    private static void initTextures() {
        if (spriteCable == null) {
            spriteCable = Minecraft.getInstance().getTextureMap().getAtlasSprite(Reference.MODID + ":blocks/constructionblock");
        }
    }

    private IBakedModel getModel(@Nonnull IBlockState state) {
        initTextures();
        return Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, Random rand) {
//        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
//        if (extendedBlockState == null)
            return Collections.emptyList();

//       @fixme: Reimplement
//
//        //ConstructionID facadeId = extendedBlockState.getValue(ConstructionBlock.FACADEID);
//        IBlockState facadeState = extendedBlockState.getValue(ConstructionBlock.FACADE_ID);
//        IBlockState extFacadeState = extendedBlockState.getValue(ConstructionBlock.FACADE_EXT_STATE);
//        IBakedModel model;
//        if (facadeState == null) {
//            return blankConstructionModel.getQuads(state, side, rand);
//        }
//
//        //IBlockState facadeState = facadeId.getBlockState();
//
//        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
//
//        if (layer != null && !facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
//            return Collections.emptyList();
//        }
//        model = getModel(facadeState);
//        try {
//            return model.getQuads(extFacadeState, side, rand);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return model.getQuads(facadeState, side, rand);
//        }
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
        return blankConstructionModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
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
}
