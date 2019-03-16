package com.direwolf20.buildinggadgets.client.gui;

final class NameDisplayData {

    private final int x;
    private final int y;
    private final boolean selected;

    public NameDisplayData(int x, int y, boolean selected) {
        this.x = x;
        this.y = y;
        this.selected = selected;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isSelected() {
        return selected;
    }

}
