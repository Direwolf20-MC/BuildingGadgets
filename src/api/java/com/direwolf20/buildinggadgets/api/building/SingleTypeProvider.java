package com.direwolf20.buildinggadgets.api.building;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

/**
 * Immutable block provider that always return the same block state regardless of which position is requested.
 */
public class SingleTypeProvider implements IBlockProvider {
    private static final String SINGLE_TYPE_PROVIDER_TAG = "tag";

    private final BlockState state;

    /**
     * @param state value that {@link #at(BlockPos)} will return
     */
    public SingleTypeProvider(BlockState state) {
        this.state = state;
    }

    @Override
    public TranslationWrapper translate(BlockPos origin) {
        return new TranslationWrapper(this, origin);
    }

    /**
     * @return {@link #state}, which is initialized in the constructor, regardless of the parameter.
     */
    @Override
    public BlockState at(BlockPos pos) {
        return state;
    }

    public BlockState getBlockState() {
        return state;
    }

    @Override
    public void serialize(CompoundNBT tag) {
        tag.put(SINGLE_TYPE_PROVIDER_TAG, NBTUtil.writeBlockState(state));
    }

    @Override
    public SingleTypeProvider deserialize(CompoundNBT tag) {
        return new SingleTypeProvider(NBTUtil.readBlockState(tag.getCompound(SINGLE_TYPE_PROVIDER_TAG)));
    }

}
