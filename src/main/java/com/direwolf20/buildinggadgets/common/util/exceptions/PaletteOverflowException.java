package com.direwolf20.buildinggadgets.common.util.exceptions;

import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;

public class PaletteOverflowException extends Exception {

    private static final long serialVersionUID = 6588933909692330592L;

    private final int amountPalettes;

    public PaletteOverflowException(IPositionPlacementSequence positions, int amountPalettes) {
        super("The number of unique block states in " + positions + " exceeded 16777216, in total there are " + amountPalettes + " of them.");
        this.amountPalettes = amountPalettes;
    }

    public int getAmountPalettes() {
        return amountPalettes;
    }

}
