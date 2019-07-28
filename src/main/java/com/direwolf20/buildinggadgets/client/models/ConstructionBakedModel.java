package com.direwolf20.buildinggadgets.client.models;

import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.tiles.ConstructionBlockTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ConstructionBakedModel implements IDynamicBakedModel {
    private BlockState facadeState;
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
        if (facadeState == null) return false;
        IBakedModel model;
        model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(facadeState);
        return model.isAmbientOcclusion();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData) {
        IBakedModel model;
        facadeState = modelData.getData(ConstructionBlockTileEntity.FACADE_STATE);
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        if (facadeState == null || facadeState == Blocks.AIR.getDefaultState())
            facadeState = BGBlocks.constructionBlockDense.getDefaultState();
        if (layer != null && ! facadeState.getBlock().canRenderInLayer(facadeState, layer)) { // always render in the null layer or the block-breaking textures don't show up
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
}
