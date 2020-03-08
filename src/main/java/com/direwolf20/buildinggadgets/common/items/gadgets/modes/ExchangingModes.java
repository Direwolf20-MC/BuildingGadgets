package com.direwolf20.buildinggadgets.common.items.gadgets.modes;

import java.util.Arrays;

public enum ExchangingModes {
    SURFACE(new SurfaceMode(true), "surface"),
    GRID(new GridMode(true), "grid"),
    VERTICAL_COLUMN(new VerticalColumnMode(true), "vertical_column"),
    HORIZONTAL_COLUMN(new HorizontalColumnMode(true), "horizontal_column");

    AbstractMode mode;
    String name;

    ExchangingModes(AbstractMode mode, String name) {
        this.mode = mode;
        this.name = name;
    }

    public AbstractMode getMode() {
        return mode;
    }

    public String getName() {
        return name;
    }

    public String getTranslationKey() {
        return "buildinggadgets.modes." + name;
    }

    public String getIcon() {
        return "textures/gui/mode/" + name;
    }
    public static ExchangingModes getFromName(String name) {
        return Arrays.stream(ExchangingModes.values())
                .filter(e -> e.toString().equals(name))
                .findFirst()
                .orElse(SURFACE);
    }
}
