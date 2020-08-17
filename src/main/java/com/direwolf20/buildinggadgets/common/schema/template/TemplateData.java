package com.direwolf20.buildinggadgets.common.schema.template;

import com.google.common.base.MoreObjects;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * This class is a mutable wrapper around {@link BlockPos position} and {@link BlockState state} it therefore represents
 * a single data point in a {@link Template}.
 */
@Immutable
public final class TemplateData {
    private final BlockPos pos;
    private final BlockData data;

    public TemplateData(BlockPos pos, BlockData data) {
        this.pos = pos.toImmutable();
        this.data = data;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockData getData() {
        return data;
    }

    public TemplateData copy() {
        return new TemplateData(pos, data.copy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof TemplateData)) return false;

        final TemplateData that = (TemplateData) o;

        if (! pos.equals(that.pos)) return false;
        return data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = pos.hashCode();
        return 31 * result + data.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pos", pos)
                .add("data", data)
                .toString();
    }

    @Immutable
    public static final class BlockData {
        private final BlockState state;
        @Nullable
        private final CompoundNBT nbt;

        public BlockData(BlockState state, @Nullable CompoundNBT nbt) {
            this.state = state;
            this.nbt = nbt;
        }

        public BlockState getState() {
            return state;
        }

        //package-private getter for serialization in Templates. Having this public is dangerous as it makes
        //Templates mutable!!!
        @Nullable
        CompoundNBT getNbt() {
            return nbt;
        }

        @Nullable
        public CompoundNBT copyNbt() {
            return nbt != null ? nbt.copy() : null;
        }

        public BlockData copy() {
            return new BlockData(state, copyNbt());
        }

        public BlockData mirror(Mirror mirror) {
            return new BlockData(state.mirror(mirror), nbt);
        }

        public BlockData rotate(Rotation rotation) {
            return new BlockData(state.rotate(rotation), nbt);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (! (o instanceof BlockData)) return false;

            final BlockData blockData = (BlockData) o;

            if (! state.equals(blockData.state)) return false;
            return nbt != null ? nbt.equals(blockData.nbt) : blockData.nbt == null;
        }

        @Override
        public int hashCode() {
            return nbt != null ? 31 * state.hashCode() + nbt.hashCode() : state.hashCode();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("state", state)
                    .add("nbt", nbt)
                    .toString();
        }
    }
}
