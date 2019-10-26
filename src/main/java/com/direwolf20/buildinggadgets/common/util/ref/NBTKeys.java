package com.direwolf20.buildinggadgets.common.util.ref;

import net.minecraft.util.ResourceLocation;

import static com.direwolf20.buildinggadgets.common.util.ref.Reference.MODID;

public final class NBTKeys {
    public static final ResourceLocation AND_SERIALIZER_ID = new ResourceLocation(MODID, "sub_entries");
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_BOUNDS = "bounds";
    public static final String KEY_CAP_COMPARISON = "cap_comp";
    public static final String KEY_CAP_NBT = "cap_data";
    public static final String KEY_COUNT = "count";
    public static final String KEY_DATA = "data";
    public static final String KEY_DATA_COMPARISON = "data_comp";
    public static final String KEY_HEADER = "header";
    public static final String KEY_ID = "id";
    public static final String KEY_MATERIALS = "materials";
    public static final String KEY_MAX_X = "maxX";
    public static final String KEY_MAX_Y = "maxY";
    public static final String KEY_MAX_Z = "maxZ";
    public static final String KEY_MIN_X = "minX";
    public static final String KEY_MIN_Y = "minY";
    public static final String KEY_MIN_Z = "minZ";
    public static final String KEY_NAME = "name";
    public static final String KEY_POS = "pos";
    public static final String KEY_SERIALIZER = "serializer";
    public static final String KEY_STATE = "state";
    public static final String KEY_SUB_ENTRIES = "sub_entries";
    public static final ResourceLocation OR_SERIALIZER_ID = new ResourceLocation(MODID, "alternatives");
    public static final ResourceLocation SIMPLE_SERIALIZER_ID = new ResourceLocation(MODID, "entries");
    private NBTKeys() {}

    public static final String POSITION_X = "X";
    public static final String POSITION_Y = "Y";
    public static final String POSITION_Z = "Z";

    public static final String CREATIVE_MARKER = "creative";

    public static final String GADGET_MODE = "mode";
    public static final String GADGET_TICKS = "ticks";
    public static final String GADGET_BUSY = "busy";
    public static final String GADGET_REPLACEMENT_BLOCK = "replacement_block";
    public static final String GADGET_SOURCE_BLOCK = "source_block";
    public static final String GADGET_USE_PASTE = "use_paste";
    public static final String GADGET_RANGE = "range";
    public static final String GADGET_OVERLAY = "overlay";
    public static final String GADGET_FUZZY = "fuzzy";
    public static final String GADGET_RAYTRACE_FLUID = "raytrace_fluid";
    public static final String GADGET_PLACE_INSIDE = "start_inside";
    public static final String GADGET_UNCONNECTED_AREA = "unconnected_area";
    public static final String GADGET_ANCHOR = "anchor";
    public static final String GADGET_ANCHOR_SIDE = "anchor_side";
    public static final String GADGET_ANCHOR_COORDS = "anchor_coords";
    public static final String GADGET_UNDO_STACK = "undo_stack";
    public static final String GADGET_UNDO_INT_COORDS = "undo_int_coords";
    public static final String GADGET_UNDO_START_POS = "undo_start_pos";
    public static final String GADGET_REL_POS = "rel_pos";
    public static final String GADGET_UUID = "uuid";
    public static final String GADGET_START_POS = "start_pos";
    public static final String GADGET_END_POS = "end_pos";
    public static final String GADGET_DIM = "dim";
    public static final String GADGET_VALUE_UP = "up";
    public static final String GADGET_VALUE_DOWN = "down";
    public static final String GADGET_VALUE_RIGHT = "right";
    public static final String GADGET_VALUE_LEFT = "left";
    public static final String GADGET_VALUE_DEPTH = "depth";
    public static final String[] GADGET_VALUES = new String[]{GADGET_VALUE_RIGHT, GADGET_VALUE_LEFT, GADGET_VALUE_UP, GADGET_VALUE_DOWN, GADGET_VALUE_DEPTH};

    /**
     * The mapping between an internal block state ID and a block state.
     */
    public static final String MAP_PALETTE = "block_palette";
    public static final String MAP_SLOT = "slot";
    public static final String MAP_STATE = "state";
    public static final String MAP_STATE_ID = "state_id";
    public static final String MAP_INT_STACK = "map_int_stack";
    /**
     * The mapping between a position index and an actual position relative to the start position.
     */
    public static final String MAP_INDEX2POS = "i2pos";
    /**
     * The mapping between a position index and an internal block state ID.
     */
    public static final String MAP_INDEX2STATE_ID = "i2state_id";
    public static final String MAP_POS_PASTE = "pos_paste_array";
    public static final String MAP_STATE_PASTE = "state_paste_array";
    public static final String MAP_SERIALIZE_KEY = "key";
    public static final String MAP_SERIALIZE_VALUE = "val";

    public static final String TEMPLATE_COPY_COUNT = "copy_count";
    public static final String TEMPLATE_UUID = GADGET_UUID;
    public static final String TEMPLATE_NAME = "template_name";
    public static final String TEMPLATE_OWNER = "owner";
    public static final String TEMPLATE_KEY_ID = "template_id";

    public static final String PASTE_COUNT = "amount";

    public static final String WORLD_SAVE_UUID = GADGET_UUID;
    public static final String WORLD_SAVE_TAG = "tag";
    public static final String WORD_SAVE_DATA_MAP = "data_map";
    public static final String WORLD_SAVE_TIME = "time";
    public static final String WORLD_SAVE_UNDO_HISTORY = "undo_history";
    public static final String WORLD_SAVE_DIM = "dim";
    public static final String WORLD_SAVE_UNDO_BLOCK_LIST = "block_list";
    public static final String WORLD_SAVE_UNDO_DATA_LIST = "data_list";
    public static final String WORLD_SAVE_UNDO_DATA_SERIALIZER_LIST = "data_serializer_list";
    public static final String WORLD_SAVE_UNDO_ITEMS_LIST = "items_list";
    public static final String WORLD_SAVE_UNDO_ITEMS_SERIALIZER_LIST = "items_serializer_list";
    public static final String WORLD_SAVE_UNDO_RECORDED_DATA = "recorded_data";
    public static final String WORLD_SAVE_UNDO_PLACED_DATA = "placed_data";
    public static final String WORLD_SAVE_UNDO_ITEMS_USED = "used_items";
    public static final String WORLD_SAVE_UNDO_ITEMS_PRODUCED = "produced_items";
    public static final String WORLD_SAVE_UNDO_BOUNDS = "bounding_box";

    public static final String REMOTE_INVENTORY_POS = "bound_te_pos";
    public static final String REMOTE_INVENTORY_DIM = "bound_te_dim";

    public static final String ENERGY = "energy";

    public static final String UNIQUE_ITEM_ITEM = "item";
    public static final String UNIQUE_ITEM_SERIALIZER = "serializer";
    public static final String UNIQUE_ITEM_COUNT = "count";

    public static final String TE_CONSTRUCTION_STATE = MAP_STATE;
    public static final String TE_CONSTRUCTION_STATE_ACTUAL = "state_actual";
    public static final String TE_TEMPLATE_MANAGER_ITEMS = "items";

    public static final String ENTITY_DESPAWNING = "despawning";
    public static final String ENTITY_TICKS_EXISTED = "ticks_existed";
    public static final String ENTITY_SET_POS = "set_pos";

    public static final String ENTITY_CONSTRUCTION_MAKING_PASTE = "makingPaste";

    public static final String ENTITY_BUILD_SET_BLOCK = "set_block";
    public static final String ENTITY_BUILD_SET_BLOCK_ACTUAL = "set_block_actual";
    public static final String ENTITY_BUILD_ORIGINAL_BLOCK = "original_block";
    public static final String ENTITY_BUILD_USE_PASTE = "use_paste";

    public static final String CHARGING_MAX_BURN = "max_burn";

    public static final String AREA = "area";

}
