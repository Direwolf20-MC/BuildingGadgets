package com.direwolf20.buildinggadgets.common.blocks.Models;

import com.direwolf20.buildinggadgets.common.utils.Reference;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;

import java.util.Set;

public class BakedModelLoader implements ICustomModelLoader {
    private static final IUnbakedModel CONSTRUCTION_MODEL = new ConstructionModel();
    private static final Set<String> NAMES = ImmutableSet.of(
            "construction_block");

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (!modelLocation.getNamespace().equals(Reference.MODID)) {
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
