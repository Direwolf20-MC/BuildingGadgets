package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.template.tilesupport.ITileEntityData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface ITransactionOperator {
    public enum Characteristics {
        TRANSFORM_STATE,
        TRANSFORM_POSITION,
        TRANSFORM_DATA
    }

    public IBlockState transformState(IBlockState state);

    public BlockPos transformPos(BlockPos pos, IBlockState state, ITileEntityData data);

    public ITileEntityData transformData(BlockPos pos, IBlockState state, ITileEntityData data);

    public Set<Characteristics> characteristics();
}
