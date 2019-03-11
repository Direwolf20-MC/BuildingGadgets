package com.direwolf20.buildinggadgets.common.utils.ref;

public final class NBTKeys {
    private NBTKeys() {}

    public static final String POSITION_X = "X";
    public static final String POSITION_Y = "Y";
    public static final String POSITION_Z = "Z";

    public static final String CREATIVE_MARKER = "creative";

    public static final String GADGET_MODE = "mode";
    public static final String GADGET_OVERLAY = "overlay";
    public static final String GADGET_FUZZY = "fuzzy";
    public static final String GADGET_RAYTRACE_FLUID = "raytrace_fluid";
    public static final String GADGET_PLACE_INSIDE = "start_inside";
    public static final String GADGET_CONNECTED_AREA = "connected_area";
    public static final String GADGET_ANCHOR = "anchor";
    public static final String GADGET_ANCHOR_SIDE = "anchor_side";
    public static final String GADGET_LAST_BUILD_POS = "last_build_pos";
    public static final String GADGET_LAST_BUILD_DIM = "last_build_dim";
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

    public static final String MAP_INT_STATE = "map_int_state";
    public static final String MAP_SLOT = "slot";
    public static final String MAP_STATE = "state";
    public static final String MAP_STATE_ID = "state_id";
    public static final String MAP_INT_STACK = "map_int_stack";
    public static final String MAP_POS_INT = "pos_int_array";
    public static final String MAP_STATE_INT = "state_int_array";
    public static final String MAP_POS_PASTE = "pos_paste_array";
    public static final String MAP_STATE_PASTE = "state_paste_array";

    public static final String TEMPLATE_COPY_COUNT = "copy_count";
    public static final String TEMPLATE_UUID = GADGET_UUID;
    public static final String TEMPLATE_NAME = "template_name";

    public static final String PASTE_COUNT = "amount";
}
