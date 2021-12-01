package com.direwolf20.buildinggadgets.common.util.ref;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import net.minecraft.resources.ResourceLocation;

// todo: remove. I Don't like how Botania does it and I don't like it here either.
//       if you want to have a string used in mutiple places, find a home for it. Magic classes
//       are not a good solution for OO

@Tainted(reason = "Awful. Contains a large amount of unused data or single-query data")
public final class Reference {
    public static final String MODID = "buildinggadgets";
    public static final String MARKER_BEFORE = MODID + ":before";
    public static final ResourceLocation MARKER_BEFORE_RL = new ResourceLocation(MARKER_BEFORE);
    public static final String MARKER_AFTER = MODID + ":after";
    public static final ResourceLocation MARKER_AFTER_RL = new ResourceLocation(MARKER_AFTER);

    public static final ResourceLocation NETWORK_CHANNEL_ID_MAIN = new ResourceLocation(Reference.MODID, "main_network_channel");
    public static final ResourceLocation WORLD_TEMPLATE_PROVIDER_ID = new ResourceLocation(MODID, "template_provider");

    private Reference() {}

    public static final class SaveReference {
        private SaveReference() {}

        public static final String TEMPLATE_SAVE_TEMPLATES = MODID + "_template_save";
        public static final String UNDO_BUILDING = MODID + "_undo_building";
        public static final String UNDO_COPY_PASTE = MODID + "_undo_copy_paste";
        public static final String UNDO_DESTRUCTION = MODID + "_undo_destruction";
        public static final String UNDO_EXCHANGING = MODID + "_undo_exchanging";
    }

    public static final class ItemReference {
        public static final ResourceLocation TAG_TEMPLATE_CONVERTIBLE = new ResourceLocation(MODID, "template_convertible");

        private ItemReference() {}
    }

    public static final class BlockReference {

        public static final class TagReference {
            public static final ResourceLocation BLACKLIST_COPY_PASTE = new ResourceLocation(MODID, "blacklist/copy_paste");
            public static final ResourceLocation BLACKLIST_BUILDING = new ResourceLocation(MODID, "blacklist/building");
            public static final ResourceLocation BLACKLIST_EXCHANGING = new ResourceLocation(MODID, "blacklist/exchanging");
            public static final ResourceLocation BLACKLIST_DESTRUCTION = new ResourceLocation(MODID, "blacklist/destruction");
            public static final ResourceLocation WHITELIST_COPY_PASTE = new ResourceLocation(MODID, "whitelist/copy_paste");
            public static final ResourceLocation WHITELIST_BUILDING = new ResourceLocation(MODID, "whitelist/building");
            public static final ResourceLocation WHITELIST_EXCHANGING = new ResourceLocation(MODID, "whitelist/exchanging");
            public static final ResourceLocation WHITELIST_DESTRUCTION = new ResourceLocation(MODID, "whitelist/destruction");

            private TagReference() {}
        }

        private BlockReference() {}
    }

    public static final class EntityReference {
        public static final String CONSTRUCTION_BLOCK_ENTITY = Reference.MODID + ":construction_block_entity";

        public static final ResourceLocation CONSTRUCTION_BLOCK_ENTITY_RL = new ResourceLocation(CONSTRUCTION_BLOCK_ENTITY);

        private EntityReference() {}
    }

    public static final class TileDataSerializerReference {
        public static final ResourceLocation REGISTRY_ID_TILE_DATA_SERIALIZER = new ResourceLocation(MODID, "tile_data/serializer");

        private TileDataSerializerReference() {}

        public static final String DUMMY_SERIALIZER = MODID + ":dummy_serializer";
        public static final ResourceLocation DUMMY_SERIALIZER_RL = new ResourceLocation(DUMMY_SERIALIZER);
        public static final String NBT_TILE_ENTITY_DATA_SERIALIZER = MODID + ":nbt_tile_data_serializer";
        public static final ResourceLocation NBT_TILE_ENTITY_DATA_SERIALIZER_RL = new ResourceLocation(NBT_TILE_ENTITY_DATA_SERIALIZER);
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
