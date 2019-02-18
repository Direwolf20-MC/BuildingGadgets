package com.direwolf20.buildinggadgets.common.building.placement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Wraps an {@link IBlockProvider} such that all access to the provider will be translated by the given amount as the
 * BlockPos passed into the constructor.
 */
public final class OriginWrapper implements IBlockProvider {

    private final IBlockProvider provider;
    private final BlockPos origin;

    /**
     * @implNote you cannot create a object and pass itself as a parameter of the constructor, so we are safe from looping
     */
    public OriginWrapper(IBlockProvider provider, BlockPos origin) {
        this.provider = provider;
        this.origin = origin;
    }

    @Override
    public BlockPos getOrigin() {
        return origin;
    }

    /**
     * Redirects the call to the wrapped IBlockProvider.
     */
    @Override
    public IBlockState at(BlockPos pos) {
        // This should be fine since the parameter is @NoBorrow
        return provider.at(pos.add(origin));
    }

}
