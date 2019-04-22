package com.direwolf20.buildinggadgets.api.building;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

/**
 * Immutable block provider that always return the same block state regardless of which position is requested.
 */
public class SingleTypeProvider implements IBlockProvider {
    private static final String SINGLE_TYPE_PROVIDER_TAG = "tag";

    private final IBlockState state;

    /**
     * @param state value that {@link #at(BlockPos)} will return
     */
    public SingleTypeProvider(IBlockState state) {
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
    public IBlockState at(BlockPos pos) {
        return state;
    }

    public IBlockState getBlockState() {
        return state;
    }

    @Override
    public void serialize(NBTTagCompound tag) {
        tag.setTag(SINGLE_TYPE_PROVIDER_TAG, NBTUtil.writeBlockState(state));
    }

    @Override
    public SingleTypeProvider deserialize(NBTTagCompound tag) {
        return new SingleTypeProvider(NBTUtil.readBlockState((NBTTagCompound) tag.getTag(SINGLE_TYPE_PROVIDER_TAG)));
    }

}
