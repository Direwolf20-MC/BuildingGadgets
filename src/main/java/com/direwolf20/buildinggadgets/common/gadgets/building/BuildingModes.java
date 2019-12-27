package com.direwolf20.buildinggadgets.common.gadgets.building;

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
    String i18n;
    BuildingModes(AbstractMode mode, String i18n) {
        this.mode = mode;
        this.i18n = "buildinggadgets.modes." + i18n;
    }

    public AbstractMode getMode() {
        return mode;
    }

    public String getI18n() {
        return i18n;
    }
}
