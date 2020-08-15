package com.direwolf20.buildinggadgets.common.schema.template;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

/**
 * This class is a mutable wrapper around {@link BlockPos position} and {@link BlockState state} it therefore represents
 * a single data point in a {@link Template}. Notice that because it is mutable this class should
 * not be stored directly when outside code has references to it.
 */
public final class TemplateData {
    private BlockPos pos;
    private BlockData data;

    public TemplateData(BlockPos pos, BlockData data) {
        setInformation(pos, data);
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockData getData() {
        return data;
    }

    public TemplateData setInformation(BlockPos pos, BlockData data) {
        this.pos = pos;
        this.data = data;
        return this;
    }

    public TemplateData copy() {
        return new TemplateData(pos.toImmutable(), data.copy());
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

    public static final class BlockData {
        private static final String KEY_STATE = "state";
        private static final String KEY_NBT = "tile";
        private final BlockState state;
        @Nullable
        private final CompoundNBT nbt;
        private final int hashCode;

        private BlockData(BlockState state, @Nullable CompoundNBT nbt, int hashCode) {
            this.state = state;
            this.nbt = nbt;
            this.hashCode = hashCode;
        }

        public BlockData(BlockState state, @Nullable CompoundNBT nbt) {
            this(state, nbt, nbt != null ? 31 * state.hashCode() + nbt.hashCode() : state.hashCode());
        }

        public static BlockData deserialize(CompoundNBT nbt) {
            if (nbt.contains(KEY_STATE, NBT.TAG_COMPOUND) && nbt.contains(KEY_NBT, NBT.TAG_COMPOUND)) {
                BlockState state = NBTUtil.readBlockState(nbt.getCompound(KEY_STATE));
                CompoundNBT tile = nbt.getCompound(KEY_NBT);
                return new BlockData(state, tile);
            }
            return new BlockData(NBTUtil.readBlockState(nbt), null);
        }

        public BlockState getState() {
            return state;
        }

        @Nullable
        public CompoundNBT copyNbt() {
            return nbt != null ? nbt.copy() : null;
        }

        public BlockData copy() {
            return new BlockData(state, nbt != null ? nbt.copy() : nbt, hashCode);
        }

        public CompoundNBT serialize() {
            if (nbt == null)
                return NBTUtil.writeBlockState(state);
            CompoundNBT res = new CompoundNBT();
            res.put(KEY_STATE, NBTUtil.writeBlockState(state));
            res.put(KEY_NBT, nbt);
            return res;
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
            if (hashCode != blockData.hashCode) return false; //short-circuit for guava set's...

            if (! state.equals(blockData.state)) return false;
            return nbt != null ? nbt.equals(blockData.nbt) : blockData.nbt == null;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
