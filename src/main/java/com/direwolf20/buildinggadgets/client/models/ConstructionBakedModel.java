package com.direwolf20.buildinggadgets.client.models;

import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.tileentities.ConstructionBlockTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class ConstructionBakedModel implements IDynamicBakedModel {
    private BlockState facadeState;

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return null;
    }

    @Override
    public boolean useAmbientOcclusion() {
        if (facadeState == null) return false;
        BakedModel model;
        model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
        return model.useAmbientOcclusion();
    }


    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData modelData, RenderType type) {
        BakedModel model;
        facadeState = modelData.get(ConstructionBlockTileEntity.FACADE_STATE);
        if (facadeState == null || facadeState == Blocks.AIR.defaultBlockState())
            facadeState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().defaultBlockState();
        model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(facadeState);
        if (type != null && !model.getRenderTypes(facadeState, rand, modelData).contains(type)) { // always render in the null layer or the block-breaking textures don't show up
            return Collections.emptyList();
        }
        return model.getQuads(facadeState, side, rand, modelData, type);

    }

//    @Override
//    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
//        return this.getBakedModel().getParticleTexture(data);
//    }

    @Override
    public ItemOverrides getOverrides() {
        return null;
    }

    @Nonnull
    @Override
    public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
        return tileData;
    }
}
