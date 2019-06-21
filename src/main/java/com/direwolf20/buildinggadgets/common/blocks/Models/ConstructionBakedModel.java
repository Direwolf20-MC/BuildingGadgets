package com.direwolf20.buildinggadgets.common.blocks.Models;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

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

    private IBakedModel getModel(@Nonnull BlockState state) {
        initTextures();
        return Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
//        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
//        if (extendedBlockState == null)
            return Collections.emptyList();

//       @fixme: Reimplement
//
//        //ConstructionID facadeId = extendedBlockState.getValue(ConstructionBlock.FACADEID);
//        BlockState facadeState = extendedBlockState.getValue(ConstructionBlock.FACADE_ID);
//        BlockState extFacadeState = extendedBlockState.getValue(ConstructionBlock.FACADE_EXT_STATE);
//        IBakedModel model;
//        if (facadeState == null) {
//            return blankConstructionModel.getQuads(state, side, rand);
//        }
//
        //        //BlockState facadeState = facadeId.getBlockData();
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
    public boolean isAmbientOcclusion(BlockState state) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        ConstructionID facadeId = extendedBlockState.getValue(ConstructionBlock.FACADEID);
        if (facadeId == null) {
            return true;
        }
        BlockState facadeState = facadeId.getBlockData();
        return facadeState.getBlock().isOpaqueCube(facadeState);
    }*/
}
