package com.direwolf20.buildinggadgets.common.schema.template;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

/**
 * This class is a mutable wrapper around {@link BlockPos position}, {@link BlockState state} and {@link CompoundNBT nbt}
 * and therefore represents a single data point in a {@link Template}. Notice that because it is mutable this class should
 * not be stored directly when outside code has references to it.
 */
public final class TemplateData {
    private BlockPos pos;
    private BlockState state;

    public TemplateData(BlockPos pos, BlockState state) {
        setInformation(pos, state);
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public TemplateData setInformation(BlockPos pos, BlockState state) {
        this.pos = Objects.requireNonNull(pos, "Cannot have BlockData with null position");
        this.state = Objects.requireNonNull(state, "Cannot have BlockData with null BlockState");
        return this;
    }

    public TemplateData copy() {
        return new TemplateData(pos.toImmutable(), state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof TemplateData)) return false;

        final TemplateData that = (TemplateData) o;

        if (! pos.equals(that.pos)) return false;
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
        int result = pos.hashCode();
        return 31 * result + state.hashCode();
    }
}
