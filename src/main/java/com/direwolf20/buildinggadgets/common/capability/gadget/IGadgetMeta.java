package com.direwolf20.buildinggadgets.common.capability.gadget;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public interface IGadgetMeta {
    BlockState getBlockState();
    void setBlockState(BlockState state);

    CompoundNBT serialize();
    void deserialize(CompoundNBT compound);
}
