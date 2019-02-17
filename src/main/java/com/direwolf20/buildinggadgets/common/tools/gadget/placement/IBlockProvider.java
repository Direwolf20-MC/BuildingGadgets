package com.direwolf20.buildinggadgets.common.tools.gadget.placement;

import com.direwolf20.buildinggadgets.common.tools.Region;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Provides the block state used for a building at some position.
 * All positions passed as parameter are relative to a specific point which can be accessed by {@link #getOrigin()}.
 */
public interface IBlockProvider {

    Region getBoundingBox();

    default BlockPos getOrigin() {
        return BlockPos.ORIGIN;
    }

    IBlockState at(@NoBorrow BlockPos pos);

}
