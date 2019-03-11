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
    public static final String GADGET_CONNECTED_AREA = "connectedArea";
    public static final String GADGET_ANCHOR = "anchor";
    public static final String GADGET_ANCHOR_SIDE = "anchorSide";
    public static final String GADGET_LAST_BUILD_POS = "lastBuild_pos";
    public static final String GADGET_LAST_BUILD_DIM = "lastBuild_dim";
    public static final String GADGET_UUID = "UUID";
    public static final String GADGET_START_POS = "startPos";
    public static final String GADGET_END_POS = "endPos";
    public static final String GADGET_DIM = "dim";
    public static final String GADGET_VALUE_UP = "up";
    public static final String GADGET_VALUE_DOWN = "down";
    public static final String GADGET_VALUE_RIGHT = "right";
    public static final String GADGET_VALUE_LEFT = "left";
    public static final String GADGET_VALUE_DEPTH = "depth";
    public static final String[] GADGET_VALUES = new String[]{GADGET_VALUE_RIGHT, GADGET_VALUE_LEFT, GADGET_VALUE_UP, GADGET_VALUE_DOWN, GADGET_VALUE_DEPTH};

    public static final String TEMPLATE_MAP_INT_STATE = "mapIntState";
    public static final String TEMPLATE_MAP_INT_STACK = "mapIntStack";
    public static final String TEMPLATE_MAP_POS_INT = "posIntArray";
    public static final String TEMPLATE_MAP_STATE_INT = "stateIntArray";
    public static final String TEMPLATE_MAP_POS_PASTE = "posPasteArray";
    public static final String TEMPLATE_MAP_STATE_PASTE = "statePasteArray";
    public static final String TEMPLATE_COPY_COUNT = "copy_counter";
    public static final String TEMPLATE_UUID = GADGET_UUID;
    public static final String TEMPLATE_NAME = "templateName";

    public static final String PASTE_COUNT = "amount";
}
