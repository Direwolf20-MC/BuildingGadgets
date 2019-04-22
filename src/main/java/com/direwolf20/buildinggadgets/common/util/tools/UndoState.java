package com.direwolf20.buildinggadgets.common.util.tools;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import java.util.List;

public class UndoState {

    public final DimensionType dimension;
    public final List<BlockPos> coordinates;

    public UndoState(DimensionType dim, List<BlockPos> coords) {
        dimension = dim;
        coordinates = coords;
    }

}
