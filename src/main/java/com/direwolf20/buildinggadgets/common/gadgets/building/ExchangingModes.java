package com.direwolf20.buildinggadgets.common.gadgets.building;

public enum ExchangingModes {
    SURFACE(new SurfaceMode(true)),
    GRID(new GridMode(true)),
    VERTICAL_COLUMN(new VerticalColumnMode(true)),
    HORIZONTAL_COLUMN(new HorizontalColumnMode(true));

    AbstractMode mode;
    ExchangingModes(AbstractMode mode) {
        this.mode = mode;
    }

    public AbstractMode getMode() {
        return mode;
    }
}
