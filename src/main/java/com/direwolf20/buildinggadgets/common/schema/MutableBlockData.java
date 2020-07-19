package com.direwolf20.buildinggadgets.common.schema;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Objects;

public final class MutableBlockData {
    private BlockPos pos;
    private BlockState state;
    @Nullable
    private CompoundNBT tileNbt;

    public MutableBlockData(BlockPos pos, BlockState state, @Nullable CompoundNBT tileNbt) {
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

    public MutableBlockData setInformation(BlockPos pos, BlockState state, @Nullable CompoundNBT tileNbt) {
        this.pos = Objects.requireNonNull(pos, "Cannot have BlockData with null position");
        this.state = Objects.requireNonNull(state, "Cannot have BlockData with null BlockState");
        this.tileNbt = tileNbt;
        return this;
    }

    public MutableBlockData setInformation(BlockPos pos, BlockState state) {
        return setInformation(pos, state, null);
    }

    public BlockData toImmutable() {
        return BlockData.create(pos, state, tileNbt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof MutableBlockData)) return false;

        final MutableBlockData that = (MutableBlockData) o;

        if (! pos.equals(that.pos)) return false;
        if (! state.equals(that.state)) return false;
        return tileNbt != null ? tileNbt.equals(that.tileNbt) : that.tileNbt == null;
    }

    @Override
    public int hashCode() {
        int result = pos.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + (tileNbt != null ? tileNbt.hashCode() : 0);
        return result;
    }
}
