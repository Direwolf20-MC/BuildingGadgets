package com.direwolf20.buildinggadgets.api.template.building;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class PlacementTarget {
    private final BlockPos pos;
    private final BlockData data;

    public PlacementTarget(@Nonnull BlockPos pos, @Nonnull BlockData data) {
        this.pos = Objects.requireNonNull(pos);
        this.data = Objects.requireNonNull(data);
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockData getData() {
        return data;
    }

    public boolean placeIn(IBuildContext context) {
        return data.placeIn(context, pos);
    }
}
