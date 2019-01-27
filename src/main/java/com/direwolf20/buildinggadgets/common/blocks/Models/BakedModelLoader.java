package com.direwolf20.buildinggadgets.common.blocks.Models;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.block.model.IUnbakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;

import java.util.Set;

public class BakedModelLoader implements ICustomModelLoader {
    private static final IUnbakedModel CONSTRUCTION_MODEL = new ConstructionModel();
    private static final Set<String> NAMES = ImmutableSet.of(
            "constructionblock");

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getNamespace().equals(BuildingGadgets.MODID)) {
            return false;
        }
        if (modelLocation instanceof ModelResourceLocation && ((ModelResourceLocation) modelLocation).getVariant().equals("inventory")) {
            return false;
        }
        return NAMES.contains(modelLocation.getPath());
    }

    @Override
    public IUnbakedModel loadModel(ResourceLocation modelLocation) throws Exception {
        return CONSTRUCTION_MODEL;
    }
}
