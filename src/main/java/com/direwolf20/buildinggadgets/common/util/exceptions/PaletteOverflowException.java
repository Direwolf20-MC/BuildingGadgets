package com.direwolf20.buildinggadgets.common.util.exceptions;

import com.direwolf20.buildinggadgets.api.building.Region;

public class PaletteOverflowException extends Exception {

    private static final long serialVersionUID = 6588933909692330592L;

    public PaletteOverflowException(Region region, int finalPalettes) {
        super("The number of unique block states in " + region + " exceeded 16777216, in total there are " + finalPalettes + " of them.");
    }

}
