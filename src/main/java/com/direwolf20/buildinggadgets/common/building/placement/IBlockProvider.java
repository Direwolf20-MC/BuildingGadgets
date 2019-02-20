package com.direwolf20.buildinggadgets.common.building.placement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Abstract representation the mapping from a position to a block state.
 * <p>
 * "abstract" means such block provider can be used for constant mapper (fill with certain type), or some sort of complex
 * structure. It does not specify a boundary and implementation should return {@code Blocks.AIR.getDefaultState()} when
 * the given position is out of boundary.
 * All positions passed as parameter are relative to a specific point which can be accessed by {@link #getTranslation()}.
 * </p>
 */
public interface IBlockProvider {

    /**
     * @param origin the new origin
     * @return <p>a new object that wraps the current block provider. All calls on {@link #at(BlockPos)} will be translated by the parameter. </p>
     * @implSpec {@code pos.add(this.getTranslation())} should be applied when accessing the current object.
     */
    default IBlockProvider translate(BlockPos origin) {
        return new OriginWrapper(this, origin);
    }

    /**
     * @return the translation used for translation done to the parameter of {@link #at(BlockPos)}
     * @implSpec the value should remain constant in the whole life of such object
     */
    default BlockPos getTranslation() {
        return BlockPos.ORIGIN;
    }

    /**
     * <p>
     * Implementations should translate the parameter by {@link #getTranslation()}.
     * </p>
     *
     * @return block that should be place at the position
     * @implNote In most cases, {@code pos.add(this.getTranslation())} should be sufficient.
     */
    IBlockState at(BlockPos pos);

}
