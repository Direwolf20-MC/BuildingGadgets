package com.direwolf20.buildinggadgets.api.util;

import net.minecraft.util.ResourceLocation;

import static com.direwolf20.buildinggadgets.api.APIReference.MODID;

public final class NBTKeys {
    public static final String KEY_MAX_X = "maxX";
    public static final String KEY_MAX_Y = "maxY";
    public static final String KEY_MAX_Z = "maxZ";
    public static final String KEY_MIN_X = "minX";
    public static final String KEY_MIN_Y = "minY";
    public static final String KEY_MIN_Z = "minZ";

    public static final String KEY_STATE = "state";
    public static final String KEY_SERIALIZER = "serializer";
    public static final String KEY_DATA = "data";
    public static final String KEY_POS = "pos";
    public static final String KEY_MATERIALS = "materials";
    public static final String KEY_COUNT = "count";
    public static final String KEY_ID = "id";
    public static final String KEY_HEADER = "header";
    public static final String KEY_BOUNDS = "bounds";
    public static final String KEY_NAME = "name";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_SUB_ENTRIES = "sub_entries";

    public static final ResourceLocation AND_SERIALIZER_ID = new ResourceLocation(MODID, "sub_entries");
    public static final ResourceLocation OR_SERIALIZER_ID = new ResourceLocation(MODID, "alternatives");
    public static final ResourceLocation SIMPLE_SERIALIZER_ID = new ResourceLocation(MODID, "entries");

    private NBTKeys() {}
}
