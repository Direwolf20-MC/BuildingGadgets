package com.direwolf20.buildinggadgets.common.utils;

import com.direwolf20.buildinggadgets.api.BuildingGadgetsApi;

public class Reference {

    public static final String MODID = BuildingGadgetsApi.MODID;
    public static final String MODNAME = "Building Gadgets";
    public static final String VERSION = "@VERSION@";
    public static final String UPDATE_JSON = "@UPDATE@";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.3.2694,)";

    public static String getLangKeyPrefix(String type, String... args) {
        return getLangKey(type, args) + ".";
    }

    public static String getLangKey(String type, String... args) {
        return String.join(".", type, MODID, String.join(".", args));
    }

}
