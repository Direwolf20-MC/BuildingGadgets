package com.direwolf20.buildinggadgets.common.blocks.Models;

import com.direwolf20.buildinggadgets.common.utils.Reference;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.model.animation.IClip;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ConstructionModel implements IUnbakedModel {
    private static final ResourceLocation MODEL_CONSTRUCTION_BLOCK = new ModelResourceLocation(Reference.MODID + ":blank_const_block");

    @Override
    public Collection<ResourceLocation> getOverrideLocations() {
        return null;
    }

    @Override
    public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
        return null;
    }

    @Nullable
    @Override
    public IBakedModel bake(Function<ResourceLocation, IUnbakedModel> modelGetter, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, IModelState state, boolean uvlock, VertexFormat format) {
        IModel constructionModel;
        try {
            constructionModel = ModelLoaderRegistry.getModel(MODEL_CONSTRUCTION_BLOCK);
        } catch (Exception e) {
            throw new Error("Unable to load construction block model", e);
        }
        return new ConstructionBakedModel(constructionModel.bake(modelGetter, spriteGetter, state, uvlock, format));
    }

//    Does not exist in 1.13
//    @Override
//    public Collection<ResourceLocation> getDependencies() {
//        List<ResourceLocation> dependencies = new ArrayList<>();
//
//        dependencies.add(MODEL_CONSTRUCTION_BLOCK);
//        return dependencies;
//    }
//


    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    @Override
    public Optional<? extends IClip> getClip(String name) {
        return Optional.empty();
    }

    @Override
    public IUnbakedModel smoothLighting(boolean value) {
        return null;
    }

    @Override
    public IUnbakedModel gui3d(boolean value) {
        return null;
    }

    @Override
    public IUnbakedModel retexture(ImmutableMap textures) {
        return null;
    }

    @Override
    public IUnbakedModel process(ImmutableMap customData) {
        return null;
    }
}
