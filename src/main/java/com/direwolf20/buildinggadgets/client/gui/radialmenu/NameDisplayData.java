package com.direwolf20.buildinggadgets.client.gui.radialmenu;

final class NameDisplayData {

    private static final char NONE = 'n';
    private static final char UNDERLINED = 'r';

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

    public char getStylePrefix() {
        return selected ? UNDERLINED : NONE;
    }

}
