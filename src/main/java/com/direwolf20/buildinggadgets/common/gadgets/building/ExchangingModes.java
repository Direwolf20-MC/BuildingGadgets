package com.direwolf20.buildinggadgets.common.gadgets.building;

public enum ExchangingModes {
    SURFACE(new SurfaceMode(true), "surface"),
    GRID(new GridMode(true), "grid"),
    VERTICAL_COLUMN(new VerticalColumnMode(true), "vertical_column"),
    HORIZONTAL_COLUMN(new HorizontalColumnMode(true), "horizontal_column");

    AbstractMode mode;
    String i18n;
    ExchangingModes(AbstractMode mode, String i18n) {
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
