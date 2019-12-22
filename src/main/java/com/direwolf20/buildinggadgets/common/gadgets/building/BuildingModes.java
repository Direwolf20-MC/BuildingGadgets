package com.direwolf20.buildinggadgets.common.gadgets.building;

public enum BuildingModes {
    BUILD_TO_ME(new BuildToMeMode()),
    VERTICAL_COLUMN(new VerticalColumnMode(false)),
    HORIZONTAL_COLUMN(new HorizontalColumnMode(false)),
    VERTICAL_WALL(new VerticalWallMode()),
    HORIZONTAL_WALL(new HorizontalWallMode()),
    STAIR(new StairMode()),
    GRID(new GridMode(false)),
    SURFACE(new SurfaceMode(false));

    AbstractMode mode;
    BuildingModes(AbstractMode mode) {
        this.mode = mode;
    }

    public AbstractMode getMode() {
        return mode;
    }
}
