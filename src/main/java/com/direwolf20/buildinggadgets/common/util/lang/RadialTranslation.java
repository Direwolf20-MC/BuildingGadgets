package com.direwolf20.buildinggadgets.common.util.lang;

import net.minecraft.client.resources.language.I18n;

public enum RadialTranslation implements ITranslationProvider {
    DESTRUCTION_OVERLAY("destruction_overlay"),
    FLUID_ONLY("fluid_only"),
    ROTATE("rotate"),
    MIRROR("mirror"),
    FUZZY("fuzzy"),
    CONNECTED_AREA("connected_area"),
    CONNECTED_SURFACE("connected_surface"),
    OPEN_GUI("open_gui"),
    OPEN_MATERIAL_LIST("open_material-list"),
    RAYTRACE_FLUID("raytrace_fluid"),
    PLACE_ON_TOP("place_on_top"),
    ANCHOR("anchor"),
    UNDO("undo");

    private final String key;

    RadialTranslation(String key) {
        this.key = "buildinggadgets.radialmenu." + key;
    }

    @Override
    public boolean areValidArguments(Object... args) {
        return true;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    public String getString() {
        return I18n.get(key);
    }
}
