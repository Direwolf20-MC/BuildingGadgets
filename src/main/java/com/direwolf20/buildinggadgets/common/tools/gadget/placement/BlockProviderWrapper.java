package com.direwolf20.buildinggadgets.common.tools.gadget.placement;

import com.direwolf20.buildinggadgets.common.tools.Region;
import com.google.common.base.Preconditions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public final class BlockProviderWrapper implements IBlockProvider {

    private final IBlockProvider provider;
    private final BlockPos origin;

    public BlockProviderWrapper(IBlockProvider provider, BlockPos origin) {
        Preconditions.checkArgument(provider != this);
        this.provider = provider;
        this.origin = origin;
    }

    @Override
    public Region getBoundingBox() {
        return provider.getBoundingBox();
    }

    @Override
    public BlockPos getOrigin() {
        return origin;
    }

    @Override
    public IBlockState at(BlockPos pos) {
        // This should be fine since the parameter is @NoBorrow
        return provider.at(pos.subtract(origin));
    }

}
