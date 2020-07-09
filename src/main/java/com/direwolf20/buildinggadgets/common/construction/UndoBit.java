package com.direwolf20.buildinggadgets.common.construction;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class UndoBit {
    private BlockPos pos;
    private BlockState state;

    public UndoBit(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    @Nullable
    public static UndoBit deserialize(CompoundNBT compound) {
        if (!compound.contains("pos") || !compound.contains("state"))
            return null;

        return new UndoBit(
                NBTUtil.readBlockPos(compound.getCompound("pos")),
                NBTUtil.readBlockState(compound.getCompound("state"))
        );
    }

    public CompoundNBT serialize() {
        CompoundNBT compound = new CompoundNBT();
        compound.put("pos", NBTUtil.writeBlockPos(this.pos));
        compound.put("state", NBTUtil.writeBlockState(this.state));
        return compound;
    }
}
