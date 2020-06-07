package com.direwolf20.buildinggadgets.common.building;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

/**
 * Abstract representation mapping from position to block state.
 * <p>
 * "abstract" means such block provider can be used for constant mapper (fill with certain type), or some sort of
 * complex structure. It does not specify a boundary and implementation should return {@code
 * Blocks.AIR.getDefaultState()} when the given position is out of boundary. All positions passed as parameter will be
 * translated by a specific vector which can be accessed by {@link #getTranslation()}.
 *
 * @param <T> {@link IBlockProvider}
 */
public interface IBlockProvider<T extends IBlockProvider<T>> {

    /**
     * @param origin the new origin
     * @return A block provider with all calls to {@link #at(BlockPos)} translated by the parameter.
     * @implSpec {@code pos.add(this.getTranslation())} should be applied when accessing the current object.
     */
    IBlockProvider translate(BlockPos origin);

    /**
     * @return the translation used as translation of the parameter of {@link #at(BlockPos)}
     * @implSpec the value should remain constant in the whole life of the object
     */
    default BlockPos getTranslation() {
        return BlockPos.ZERO;
    }

    /**
     * The parameter will be translated by {@link #getTranslation} before being used to read a block state.
     * @param pos block pos
     *
     * @return block that should be placed at the position
     * @implNote In most cases, {@code pos.add(this.getTranslation())} should be sufficient.
     */
    BlockData at(BlockPos pos);

    /**
     * Write the containing data into the given tag.
     *
     * @param tag given tag
     */
    void serialize(CompoundNBT tag);

    /**
     * @see #serialize(CompoundNBT) - create a new {@link CompoundNBT} instead of writing into an existing one.
     * @return CompoundNBT
     */
    default CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        this.serialize(tag);
        return tag;
    }

    /**
     * Reads the data contained in the given tag and write them into a new object.
     * @param tag given tag
     * @implSpec The returning object should have the same type.
     * @return {@link T}
     */
    T deserialize(CompoundNBT tag);

}
