package com.direwolf20.buildinggadgets.Tools;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class UndoState {

    public final int dimension;
    public final ArrayList<BlockPos> coordinates;

    public UndoState(int dim, ArrayList<BlockPos> coords) {
        dimension = dim;
        coordinates = coords;
    }

}
