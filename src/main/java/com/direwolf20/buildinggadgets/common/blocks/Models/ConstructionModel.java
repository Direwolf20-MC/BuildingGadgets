package com.direwolf20.buildinggadgets.common.blocks.Models;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.model.animation.IClip;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ConstructionModel implements IModel {
    private static final ResourceLocation MODEL_CONSTRUCTION_BLOCK = new ModelResourceLocation(BuildingGadgets.MODID + ":blankconstblock");

//    Does not exist in 1.13
//    @Override
//    public Collection<ResourceLocation> getDependencies() {
//        List<ResourceLocation> dependencies = new ArrayList<>();
//
//        dependencies.add(MODEL_CONSTRUCTION_BLOCK);
//        return dependencies;
//    }
//


    @Nullable
    @Override
    public IBakedModel bake(Function modelGetter, Function spriteGetter, IModelState state, boolean uvlock, VertexFormat format) {
        IModel constructionModel;
        try {
            constructionModel = ModelLoaderRegistry.getModel(MODEL_CONSTRUCTION_BLOCK);
        } catch (Exception e) {
            throw new Error("Unable to load construction block model", e);
        }
        return new ConstructionBakedModel(constructionModel.bake(modelGetter, spriteGetter, state, uvlock, format));
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
    public IModel smoothLighting(boolean value) {
        return null;
    }

    @Override
    public IModel gui3d(boolean value) {
        return null;
    }

    @Override
    public IModel retexture(ImmutableMap textures) {
        return null;
    }

    @Override
    public IModel process(ImmutableMap customData) {
        return null;
    }
}
