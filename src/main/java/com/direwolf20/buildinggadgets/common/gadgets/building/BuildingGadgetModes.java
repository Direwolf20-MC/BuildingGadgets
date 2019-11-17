package com.direwolf20.buildinggadgets.common.gadgets.building;

public enum BuildingGadgetModes {
    BUILD_TO_ME(new BuildToMeMode());

    AbstractMode mode;
    BuildingGadgetModes(AbstractMode mode) {
        this.mode = mode;
    }

    public AbstractMode getMode() {
        return mode;
    }
}
