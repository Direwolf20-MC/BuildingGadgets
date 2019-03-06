package com.direwolf20.buildinggadgets.api.template;

import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface ITransactionOperator {
    public enum Characteristics {
        TRANSFORM_POSITION,
        TRANSFORM_DATA
    }

    public BlockPos transformPos(BlockPos pos, BlockData data);

    public BlockData transformData(BlockData data);

    public Set<Characteristics> characteristics();
}
