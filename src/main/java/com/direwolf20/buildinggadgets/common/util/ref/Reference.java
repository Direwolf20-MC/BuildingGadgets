package com.direwolf20.buildinggadgets.common.util.ref;

import net.minecraft.util.ResourceLocation;

public final class Reference {
    public static final String MODID = "buildinggadgets";
    public static final String MARKER_BEFORE = MODID + ":before";
    public static final ResourceLocation MARKER_BEFORE_RL = new ResourceLocation(MARKER_BEFORE);
    public static final String MARKER_AFTER = MODID + ":after";
    public static final ResourceLocation MARKER_AFTER_RL = new ResourceLocation(MARKER_AFTER);
    public static final ResourceLocation NETWORK_CHANNEL_ID_MAIN = new ResourceLocation(Reference.MODID, "main_network_channel");
    public static final ResourceLocation NETWORK_CHANNEL_ID_LOGIN = new ResourceLocation(Reference.MODID, "login_network_channel");
    public static final String PAYLOAD_CONTEXT_ID_SYNC = "template_id_sync";

    public static final ResourceLocation WORLD_TEMPLATE_PROVIDER_ID = new ResourceLocation(MODID, "template_provider");

    public static final ResourceLocation CONDITION_PASTE_ID = new ResourceLocation(Reference.MODID, "enable_paste");
    public static final ResourceLocation CONDITION_DESTRUCTION_ID = new ResourceLocation(Reference.MODID, "enable_destruction");

    public static final ResourceLocation PROPERTY_OVERRIDE_LEVEL = new ResourceLocation("level");

    private Reference() {}

    public static final class SaveReference {

        private SaveReference() {}

        public static final String FILE_ENDING_TEMPLATE = ".template";
        public static final String FILE_ENDING_TEMPLATE_HEADER = ".th";
        public static final String TEMPLATE_SAVE_TEMPLATES = MODID + "_template_save";
        public static final String UNDO_BUILDING = MODID + "_undo_building";
        public static final String UNDO_COPY_PASTE = MODID + "_undo_copy_paste";
        public static final String UNDO_DESTRUCTION = MODID + "_undo_destruction";
        public static final String UNDO_EXCHANGING = MODID + "_undo_exchanging";
    }

    public static final class ItemReference {
        // Gadgets
        public static final String GADGET_BUILDING = MODID + ":gadget_building";
        public static final String GADGET_COPY_PASTE = MODID + ":gadget_copy_paste";
        public static final String GADGET_DESTRUCTION = MODID + ":gadget_destruction";
        public static final String GADGET_EXCHANGING = MODID + ":gadget_exchanging";
        // Building Items
        public static final String CONSTRUCTION_PASTE = MODID + ":construction_paste";
        public static final String CONSTRUCTION_CHUNK_DENSE = MODID + ":construction_chunk_dense";
        public static final String TEMPLATE = MODID + ":template";
        // Construction Paste Containers
        public static final String PASTE_CONTAINER_T1 = MODID + ":construction_paste_container_t1";
        public static final String PASTE_CONTAINER_T2 = MODID + ":construction_paste_container_t2";
        public static final String PASTE_CONTAINER_T3 = MODID + ":construction_paste_container_t3";
        public static final String PASTE_CONTAINER_CREATIVE = MODID + ":construction_paste_container_creative";

        // Gadgets
        public static final ResourceLocation GADGET_BUILDING_RL = new ResourceLocation(GADGET_BUILDING);
        public static final ResourceLocation GADGET_COPY_PASTE_RL = new ResourceLocation(GADGET_COPY_PASTE);
        public static final ResourceLocation GADGET_DESTRUCTION_RL = new ResourceLocation(GADGET_DESTRUCTION);
        public static final ResourceLocation GADGET_EXCHANGING_RL = new ResourceLocation(GADGET_EXCHANGING);
        // Building Items
        public static final ResourceLocation CONSTRUCTION_PASTE_RL = new ResourceLocation(CONSTRUCTION_PASTE);
        public static final ResourceLocation CONSTRUCTION_CHUNK_DENSE_RL = new ResourceLocation(CONSTRUCTION_CHUNK_DENSE);
        public static final ResourceLocation TEMPLATE_RL = new ResourceLocation(TEMPLATE);
        // Construction Paste Containers
        public static final ResourceLocation PASTE_CONTAINER_T1_RL = new ResourceLocation(PASTE_CONTAINER_T1);
        public static final ResourceLocation PASTE_CONTAINER_T2_RL = new ResourceLocation(PASTE_CONTAINER_T2);
        public static final ResourceLocation PASTE_CONTAINER_T3_RL = new ResourceLocation(PASTE_CONTAINER_T3);
        public static final ResourceLocation PASTE_CONTAINER_CREATIVE_RL = new ResourceLocation(PASTE_CONTAINER_CREATIVE);

        public static final ResourceLocation TAG_TEMPLATE_CONVERTIBLE = new ResourceLocation(MODID, "template_convertible");

        private ItemReference() {}
    }

    public static final class BlockReference {
        public static final String EFFECT_BLOCK = MODID + ":effect_block";
        public static final String CONSTRUCTION_BLOCK = MODID + ":construction_block";
        public static final String CONSTRUCTION_BLOCK_DENSE = MODID + ":construction_block_dense";
        public static final String CONSTRUCTION_BLOCK_POWDER = MODID + ":construction_block_powder";
        public static final String TEMPLATE_MANAGER = MODID + ":template_manager";
        public static final String CHARGING_STATION = MODID + ":charging_station";

        public static final ResourceLocation EFFECT_BLOCK_RL = new ResourceLocation(EFFECT_BLOCK);
        public static final ResourceLocation CONSTRUCTION_BLOCK_RL = new ResourceLocation(CONSTRUCTION_BLOCK);
        public static final ResourceLocation CONSTRUCTION_BLOCK_DENSE_RL = new ResourceLocation(CONSTRUCTION_BLOCK_DENSE);
        public static final ResourceLocation CONSTRUCTION_BLOCK_POWDER_RL = new ResourceLocation(CONSTRUCTION_BLOCK_POWDER);
        public static final ResourceLocation TEMPLATE_MANAGER_RL = new ResourceLocation(TEMPLATE_MANAGER);
        public static final ResourceLocation CHARGING_STATION_RL = new ResourceLocation(CHARGING_STATION);

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

    public static final class TileEntityReference {
        public static final String CONSTRUCTION_TILE = Reference.MODID + ":construction_tile";
        public static final String TEMPLATE_MANAGER_TILE = Reference.MODID + ":template_manager_tile";
        public static final String CHARGING_STATION_TILE = Reference.MODID + ":charging_station_tile";
        public static final String EFFECT_BLOCK_TILE = Reference.MODID + ":effect_block_tile";

        public static final ResourceLocation TEMPLATE_MANAGER_TILE_RL = new ResourceLocation(TEMPLATE_MANAGER_TILE);
        public static final ResourceLocation CONSTRUCTION_TILE_RL = new ResourceLocation(CONSTRUCTION_TILE);
        public static final ResourceLocation CHARGING_STATION_TILE_RL = new ResourceLocation(CHARGING_STATION_TILE);
        public static final ResourceLocation EFFECT_BLOCK_TILE_RL = new ResourceLocation(EFFECT_BLOCK_TILE);

        private TileEntityReference() {}
    }

    public static final class ContainerReference {
        public static final String TEMPLATE_MANAGER_CONTAINER = Reference.MODID + ":template_manager_container";
        public static final String CHARGING_STATION_CONTAINER = Reference.MODID + ":charging_station_container";

        public static final ResourceLocation TEMPLATE_MANAGER_CONTAINER_RL = new ResourceLocation(TEMPLATE_MANAGER_CONTAINER);
        public static final ResourceLocation CHARGING_STATION_CONTAINER_RL = new ResourceLocation(CHARGING_STATION_CONTAINER);

        private ContainerReference() {}
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
