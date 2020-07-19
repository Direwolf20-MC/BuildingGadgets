package com.direwolf20.buildinggadgets.common.schema;

import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Objects;

public final class BlockData {
    public static final BlockData create(BlockPos pos, BlockState state, @Nullable CompoundNBT nbt) {
        pos = Objects.requireNonNull(pos, "Cannot construct BlockData with null position.").toImmutable();
        return new BlockData(new MutableBlockData(pos, state, nbt));
    }

    private final MutableBlockData delegate;
    private final int hash;

    private BlockData(MutableBlockData delegate) {
        this.delegate = delegate;
        this.hash = delegate.hashCode();
    }

    public BlockPos getPos() {
        return delegate.getPos();
    }

    public BlockState getState() {
        return delegate.getState();
    }

    @Nullable
    public CompoundNBT getTileNbt() {
        return delegate.getTileNbt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (hashCode() != o.hashCode() || ! (o instanceof BlockData)) return false;

        final BlockData blockData = (BlockData) o;

        return blockData.delegate.equals(delegate);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("delegate", delegate)
                .toString();
    }
}
