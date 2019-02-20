package com.direwolf20.buildinggadgets.common.building.placement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Wraps an {@link IBlockProvider} such that all access to the handle will be translated by the given amount as the
 * BlockPos passed into the constructor.
 */
public final class OriginWrapper implements IBlockProvider {

    private final IBlockProvider handle;
    private final BlockPos translation;

    /**
     * @implNote you cannot create a object and pass itself as a parameter of the constructor, so we are safe from looping
     */
    public OriginWrapper(IBlockProvider handle, BlockPos origin) {
        this.handle = handle;
        this.translation = origin;
    }

    @Override
    public BlockPos getTranslation() {
        return translation;
    }

    /**
     * Calculate and return all translation done with the wrapper and its underlying block provider.
     *
     * @implNote this method uses recursion to stack the translations together, which means it will create a lot of overhead.
     * @return a new BlockPos every time
     */
    public BlockPos getAccumulatedTranslation() {
        return translation.add(handle.getTranslation());
    }

    /**
     * The underlying block provider that was wrapped in the constructor.
     */
    public IBlockProvider getHandle() {
        return handle;
    }

    /**
     * Redirects the call to the wrapped IBlockProvider.
     */
    @Override
    public IBlockState at(BlockPos pos) {
        return handle.at(pos.add(translation));
    }

}