package com.direwolf20.buildinggadgets.common.items.gadgets;

public class GadgetAbilities {
    public static final GadgetAbilities NONE = new GadgetAbilities(false, false, false, false, false, false, false);

    private final boolean changeModes;
    private final boolean selectBlocks;
    private final boolean changeRange;
    private final boolean canTraceWater;
    private final boolean placeOnTop;
    private final boolean canFuzzy;
    private final boolean canLink;

    public GadgetAbilities(boolean changeModes, boolean selectBlocks, boolean changeRange, boolean canTraceWater, boolean placeOnTop, boolean canFuzzy, boolean canLink) {
        this.changeModes = changeModes;
        this.selectBlocks = selectBlocks;
        this.changeRange = changeRange;
        this.canTraceWater = canTraceWater;
        this.placeOnTop = placeOnTop;
        this.canFuzzy = canFuzzy;
        this.canLink = canLink;
    }

    public boolean canChangeModes() {
        return changeModes;
    }

    public boolean isSelectBlocks() {
        return selectBlocks;
    }

    public boolean canChangeRange() {
        return changeRange;
    }

    public boolean canTraceWater() {
        return canTraceWater;
    }

    public boolean canPlaceOnTop() {
        return placeOnTop;
    }

    public boolean canFuzzy() {
        return canFuzzy;
    }

    public boolean canLink() {
        return canLink;
    }
}
