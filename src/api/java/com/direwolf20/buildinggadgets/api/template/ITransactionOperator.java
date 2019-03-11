package com.direwolf20.buildinggadgets.api.template;

import com.google.common.collect.ImmutableSet;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Represents an Operation which can be performed by an Transaction in order to change a given {@link ITemplate}.
 * It can provide the following 3 Operations:
 * <ul>
 *     <li>Transforming or removing Block-Positions</li>
 *     <li>Transforming or removing Block-Data</li>
 *     <li>Attaching new Blocks and the Data to this Template.</li>
 * </ul>
 */
public interface ITransactionOperator {
    enum Characteristics {
        TRANSFORM_POSITION,
        TRANSFORM_DATA,
        CREATE_DATA
    }

    default BlockPos transformPos(BlockPos pos, BlockData data) {
        return pos;
    }

    default BlockData transformData(BlockData data) {
        return data;
    }

    @Nullable
    default BlockPos createPos() {
        return null;
    }

    default BlockData createDataForPos(BlockPos pos) {
        throw new UnsupportedOperationException("Default implementation does not support creating BlockData!");
    }

    default Set<Characteristics> characteristics() {
        return ImmutableSet.of();
    }
}
