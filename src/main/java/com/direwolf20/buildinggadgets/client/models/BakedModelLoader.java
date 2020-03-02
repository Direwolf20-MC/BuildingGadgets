package com.direwolf20.buildinggadgets.client.models;

// 1.14
//public class BakedModelLoader implements IModelLoader {
//    private static final IUnbakedModel CONSTRUCTION_MODEL = new ConstructionModel();
//    private static final Set<String> NAMES = ImmutableSet.of(
//            "construction_block");
//
//    @Override
//    public void onResourceManagerReload(IResourceManager resourceManager) {
//
//    }
//
//    @Override
//    public IModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
//        return null;
//    }
//
//
//
////    @Override
////    public void onResourceManagerReload(IResourceManager resourceManager) {
////
////    }
////
////    @Override
////    public boolean accepts(ResourceLocation modelLocation) {
////        if (!modelLocation.getNamespace().equals(Reference.MODID)) {
////            return false;
////        }
////        if (modelLocation instanceof ModelResourceLocation && ((ModelResourceLocation) modelLocation).getVariant().equals("inventory")) {
////            return false;
////        }
////        return NAMES.contains(modelLocation.getPath());
////    }
////
////    @Override
////    public IUnbakedModel loadModel(ResourceLocation modelLocation) throws Exception {
////        return CONSTRUCTION_MODEL;
////    }
//}
