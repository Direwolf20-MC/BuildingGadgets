package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.template.tilesupport.ITileEntityData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class PlacementTarget {
    private final BlockPos pos;
    private final IBlockState state;
    private final ITileEntityData tileData;

    public PlacementTarget(@Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable ITileEntityData data) {
        this.pos = Objects.requireNonNull(pos);
        this.state = Objects.requireNonNull(state);
        this.tileData = data;
    }

    public BlockPos getPos() {
        return pos;
    }

    public IBlockState getState() {
        return state;
    }

    public ITileEntityData getTileData() {
        return tileData;
    }

    public boolean placeIn(IWorldWriter world) {
        return false;
    }
}
