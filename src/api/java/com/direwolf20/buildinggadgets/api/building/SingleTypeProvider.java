package com.direwolf20.buildinggadgets.api.building;

import com.direwolf20.buildinggadgets.api.abstraction.BlockData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

/**
 * Immutable block provider that always return the same block data regardless of which position is requested.
 */
public class SingleTypeProvider implements IBlockProvider {
    private static final String SINGLE_TYPE_PROVIDER_TAG = "tag";

    private final BlockData data;

    /**
     * @param data value that {@link #at(BlockPos)} will return
     */
    public SingleTypeProvider(BlockData data) {
        this.data = data;
    }

    @Override
    public TranslationWrapper translate(BlockPos origin) {
        return new TranslationWrapper(this, origin);
    }

    /**
     * @return {@link #data}, which is initialized in the constructor, regardless of the parameter.
     */
    @Override
    public BlockData at(BlockPos pos) {
        return data;
    }

    public BlockData getBlockData() {
        return data;
    }

    @Override
    public void serialize(CompoundNBT tag) {
        tag.put(SINGLE_TYPE_PROVIDER_TAG, data.serialize(true));
    }

    @Override
    public SingleTypeProvider deserialize(CompoundNBT tag) {
        return new SingleTypeProvider(BlockData.deserialize(tag.getCompound(SINGLE_TYPE_PROVIDER_TAG), true));
    }

}
