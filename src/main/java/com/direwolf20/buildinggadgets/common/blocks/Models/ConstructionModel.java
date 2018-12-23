package com.direwolf20.buildinggadgets.common.blocks.Models;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ConstructionModel implements IModel {
    private static final ResourceLocation MODEL_CONSTRUCTION_BLOCK = new ModelResourceLocation(BuildingGadgets.MODID + ":blankconstblock");

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IModel constructionModel;
        try {
            constructionModel = ModelLoaderRegistry.getModel(MODEL_CONSTRUCTION_BLOCK);
        } catch (Exception e) {
            throw new Error("Unable to load construction block model", e);
        }
        return new ConstructionBakedModel(constructionModel.bake(state, format, bakedTextureGetter));
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        List<ResourceLocation> dependencies = new ArrayList<>();

        dependencies.add(MODEL_CONSTRUCTION_BLOCK);
        return dependencies;
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}
