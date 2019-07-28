package com.direwolf20.buildinggadgets.client.models;

import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.model.animation.IClip;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class ConstructionModel implements IUnbakedModel {
    private static final ResourceLocation MODEL_CONSTRUCTION_BLOCK = new ModelResourceLocation(Reference.MODID + ":blank_const_block");

    @Override
    public Collection<ResourceLocation> getDependencies() {
        List<ResourceLocation> dependencies = new ArrayList<>();
        dependencies.add(MODEL_CONSTRUCTION_BLOCK);
        return dependencies;
    }

    @Nullable
    @Override
    public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format) {
        IModel constructionModel;
        try {
            constructionModel = ModelLoaderRegistry.getModel(MODEL_CONSTRUCTION_BLOCK);
        } catch (Exception e) {
            throw new Error("Unable to load construction block model", e);
        }
        return new ConstructionBakedModel();//new ConstructionBakedModel(constructionModel.bake(bakery, spriteGetter, sprite, format));
    }

    @Override
    public Collection<ResourceLocation> getTextures(Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors) {
        return Collections.emptyList();
    }


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
