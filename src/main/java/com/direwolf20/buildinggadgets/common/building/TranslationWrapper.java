package com.direwolf20.buildinggadgets.common.building;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Wraps an {@link IBlockProvider} such that all access to the provider will be translated by the given amount as the
 * BlockPos passed into the constructor.
 */
public final class TranslationWrapper implements IBlockProvider {

    private final IBlockProvider provider;
    /**
     * Translation done by the current wrapper.
     */
    private final BlockPos translation;
    /**
     * Translation done by the current wrapper and its provider.
     */
    private final BlockPos accumulatedTranslation;

    public TranslationWrapper(IBlockProvider provider, BlockPos origin) {
        this.provider = provider;
        this.translation = origin;
        this.accumulatedTranslation = provider.getTranslation().add(origin);
    }

    @Override
    public IBlockProvider translate(BlockPos origin) {
        // Since provider is the same, just adding the two translation together would be sufficient
        return new TranslationWrapper(provider, translation.add(origin));
    }

    /**
     * @return all translation done with the wrapper and its underlying block provider.
     */
    @Override
    public BlockPos getTranslation() {
        return accumulatedTranslation;
    }

    /**
     * @return the translation applied by the current wrapper
     */
    public BlockPos getAppliedTranslation() {
        return translation;
    }

    /**
     * The underlying block provider that was wrapped in the constructor.
     */
    public IBlockProvider getProvider() {
        return provider;
    }

    /**
     * Redirects the call to the wrapped IBlockProvider.
     */
    @Override
    public IBlockState at(BlockPos pos) {
        return provider.at(pos.add(translation));
    }

    @Override
    public void serialize(NBTTagCompound tag) {
        this.provider.serialize(tag);
    }

    @Override
    public TranslationWrapper deserialize(NBTTagCompound tag) {
        this.provider.deserialize(tag);
        return this;
    }

}
