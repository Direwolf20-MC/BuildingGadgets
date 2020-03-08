package com.direwolf20.buildinggadgets.common.items.gadgets.modes;

import java.util.Arrays;

public enum BuildingModes {
    BUILD_TO_ME(new BuildToMeMode(), "build_to_me"),
    VERTICAL_COLUMN(new VerticalColumnMode(false), "vertical_column"),
    HORIZONTAL_COLUMN(new HorizontalColumnMode(false), "horizontal_column"),
    VERTICAL_WALL(new VerticalWallMode(), "vertical_wall"),
    HORIZONTAL_WALL(new HorizontalWallMode(), "horizontal_wall"),
    STAIR(new StairMode(), "stairs"),
    GRID(new GridMode(false), "grid"),
    SURFACE(new SurfaceMode(false), "surface");

    AbstractMode mode;
    String name;
    BuildingModes(AbstractMode mode, String name) {
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

    public static BuildingModes getFromName(String name) {
        return Arrays.stream(BuildingModes.values())
                .filter(e -> e.toString().equals(name))
                .findFirst()
                .orElse(BUILD_TO_ME);
    }
}
