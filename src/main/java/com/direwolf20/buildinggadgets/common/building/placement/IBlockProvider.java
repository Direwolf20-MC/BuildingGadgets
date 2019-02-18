package com.direwolf20.buildinggadgets.common.building.placement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Abstract representation the mapping from a position to a block state.
 * <p>
 * "abstract" means such block provider can be used for constant mapper (fill with certain type), or some sort of complex
 * structure. It does not specify a boundary and implementation should return {@code Blocks.AIR.getDefaultState()} when
 * the given position is out of boundary.
 * All positions passed as parameter are relative to a specific point which can be accessed by {@link #getOrigin()}.
 * </p>
 * <p>
 * Implementations should kept the object
 * </p>
 */
public interface IBlockProvider {

    /**
     * @param origin the new origin
     * @return <p>a new object that wraps the current block provider. All calls on {@link #at(BlockPos)} will be translated by the parameter. </p>
     * <p>{@code pos.add(this.getOrigin())}</p>
     */
    default IBlockProvider origin(BlockPos origin) {
        return new OriginWrapper(this, origin);
    }

    /**
     * @return the origin used for translation done to the parameter of {@link #at(BlockPos)}
     * @implSpec the value should remain constant in the whole life of such object
     */
    default BlockPos getOrigin() {
        return BlockPos.ORIGIN;
    }

    /**
     * <p>
     * Implementations should translate the parameter by {@link #getOrigin()}.
     * In most cases, {@code pos.add(this.getOrigin())} should be sufficient.
     * </p>
     * <p>
     * The {@link NoBorrow} annotation for the parameter is to ensure that implementations can work with optimization done
     * with {@link AbstractTargetIterator} where the resulting position is a direct reference to the internal counter.
     * </p>
     *
     * @return block that should be place at the position
     */
    IBlockState at(@NoBorrow BlockPos pos);

}
