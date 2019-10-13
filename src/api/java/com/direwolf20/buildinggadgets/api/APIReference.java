package com.direwolf20.buildinggadgets.api;

import net.minecraft.util.ResourceLocation;

public final class APIReference {
    private APIReference() {}

    public static final String MODID = "buildinggadgets";

    public static final String MARKER_AFTER = MODID + ":after";
    public static final ResourceLocation MARKER_AFTER_RL = new ResourceLocation(MARKER_AFTER);
    public static final String MARKER_BEFORE = MODID + ":before";
    public static final ResourceLocation MARKER_BEFORE_RL = new ResourceLocation(MARKER_BEFORE);

    public static final class TileDataSerializerReference {
        public static final ResourceLocation REGISTRY_ID_TILE_DATA_SERIALIZER = new ResourceLocation(MODID, "tile_data/serializer");

        private TileDataSerializerReference() {}

        public static final String DUMMY_SERIALIZER = MODID + ":dummy_serializer";
        public static final ResourceLocation DUMMY_SERIALIZER_RL = new ResourceLocation(DUMMY_SERIALIZER);
        public static final String NBT_TILE_ENTITY_DATA_SERIALIZER = MODID + ":nbt_tile_data_serializer";
        public static final ResourceLocation NBT_TILE_ENTITY_DATA_SERIALIZER_RL = new ResourceLocation(NBT_TILE_ENTITY_DATA_SERIALIZER);
    }

    public static final class TemplateSerializerReference {
        public static final ResourceLocation REGISTRY_ID_TEMPLATE_SERIALIZER = new ResourceLocation(MODID, "template/serializer");

        private TemplateSerializerReference() {}

        public static final String IMMUTABLE_TEMPLATE_SERIALIZER = MODID + ":immutable_template_serializer";
        public static final ResourceLocation IMMUTABLE_TEMPLATE_SERIALIZER_RL = new ResourceLocation(IMMUTABLE_TEMPLATE_SERIALIZER);
        public static final String DELEGATING_TEMPLATE_SERIALIZER = MODID + ":delegating_template_serializer";
        public static final ResourceLocation DELEGATING_TEMPLATE_SERIALIZER_RL = new ResourceLocation(DELEGATING_TEMPLATE_SERIALIZER);

    }

    public static final class UniqueObjectSerializerReference {
        public static final ResourceLocation REGISTRY_ID_UNIQUE_OBJECT_SERIALIZER = new ResourceLocation(MODID, "unique_object/serializer");

        private UniqueObjectSerializerReference() {}

        public static final String SIMPLE_UNIQUE_ITEM_ID = MODID + ":simple_item";
        public static final ResourceLocation SIMPLE_UNIQUE_ITEM_ID_RL = new ResourceLocation(SIMPLE_UNIQUE_ITEM_ID);
    }

    public static final class TileDataFactoryReference {
        public static final String IMC_METHOD_TILEDATA_FACTORY = "imc_tile_data_factory";

        private TileDataFactoryReference() {}

        public static final String DATA_PROVIDER_FACTORY = MODID + ":data_provider_factory";
        public static final ResourceLocation DATA_PROVIDER_FACTORY_RL = new ResourceLocation(DATA_PROVIDER_FACTORY);
    }

    public static final class HandleProviderReference {
        public static final String IMC_METHOD_HANDLE_PROVIDER = "imc_handle_provider";

        private HandleProviderReference() {}

        public static final String STACK_HANDLER_ITEM_HANDLE = MODID + ":stack_handler_provider";
        public static final ResourceLocation STACK_HANDLER_ITEM_HANDLE_RL = new ResourceLocation(STACK_HANDLER_ITEM_HANDLE);
    }

}
