package com.direwolf20.buildinggadgets.common.schema.template;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This class is a mutable wrapper around {@link BlockPos position}, {@link BlockState state} and {@link CompoundNBT nbt}
 * and therefore represents a single data point in a {@link Template}. Notice that because it is mutable this class should
 * not be stored directly when outside code has references to it.
 */
public final class TemplateData {
    private BlockPos pos;
    private BlockState state;
    @Nullable
    private CompoundNBT tileNbt;

    public TemplateData(BlockPos pos, BlockState state, @Nullable CompoundNBT tileNbt) {
        setInformation(pos, state, tileNbt);
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    @Nullable
    public CompoundNBT getTileNbt() {
        return tileNbt;
    }

    public TemplateData setInformation(BlockPos pos, BlockState state, @Nullable CompoundNBT tileNbt) {
        this.pos = Objects.requireNonNull(pos, "Cannot have BlockData with null position");
        this.state = Objects.requireNonNull(state, "Cannot have BlockData with null BlockState");
        this.tileNbt = tileNbt;
        return this;
    }

    public TemplateData setInformation(BlockPos pos, BlockState state) {
        return setInformation(pos, state, null);
    }

    public TemplateData copy() {
        return new TemplateData(pos.toImmutable(), state, tileNbt != null ? tileNbt.copy() : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof TemplateData)) return false;

        final TemplateData that = (TemplateData) o;

        if (! pos.equals(that.pos)) return false;
        if (! state.equals(that.state)) return false;
        return Objects.equals(tileNbt, that.tileNbt);
    }

    @Override
    public int hashCode() {
        int result = pos.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + (tileNbt != null ? tileNbt.hashCode() : 0);
        return result;
    }
}
