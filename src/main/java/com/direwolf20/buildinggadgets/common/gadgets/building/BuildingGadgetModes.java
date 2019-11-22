package com.direwolf20.buildinggadgets.common.gadgets.building;

public enum BuildingGadgetModes {
    BUILD_TO_ME(new BuildToMeMode()),
    SURFACE(new SurfaceMode()),
    GRID(new GridMode(false)),
    STAIR(new StairMode()),
    VERTICAL_COLUMN(new VerticalColumnMode(false)),
    VERTICAL_WALL(new VerticalWallMode()),
    HORIZONTAL_COLUMN(new HorizontalColumnMode(false)),
    HORIZONTAL_WALL(new HorizontalWallMode());

    AbstractMode mode;
    BuildingGadgetModes(AbstractMode mode) {
        this.mode = mode;
    }

    public AbstractMode getMode() {
        return mode;
    }
}
