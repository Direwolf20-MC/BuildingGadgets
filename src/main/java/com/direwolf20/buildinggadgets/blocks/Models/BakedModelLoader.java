package com.direwolf20.buildinggadgets.blocks.Models;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.util.Set;

public class BakedModelLoader implements ICustomModelLoader {
    private static final ConstructionModel CONSTRUCTION_MODEL = new ConstructionModel();
    private static final Set<String> NAMES = ImmutableSet.of(
            "constructionblock");

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getResourceDomain().equals(BuildingGadgets.MODID)) {
            return false;
        }
        if (modelLocation instanceof ModelResourceLocation && ((ModelResourceLocation) modelLocation).getVariant().equals("inventory")) {
            return false;
        }
        return NAMES.contains(modelLocation.getResourcePath());
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        return CONSTRUCTION_MODEL;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }
}
