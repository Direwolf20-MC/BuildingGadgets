package com.direwolf20.buildinggadgets.api.template;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

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

    public boolean placeIn(IWorld world) {
        return false;
    }
}
