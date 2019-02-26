package com.direwolf20.buildinggadgets.util;

import com.direwolf20.buildinggadgets.common.building.Region;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class CasedBlockView extends RegionBlockView {

    private IBlockState otherState;
    private Set<BlockPos> otherPositions;

    public CasedBlockView(Region region, IBlockState state, IBlockState otherState) {
        super(region, state);
        this.otherState = otherState;
        this.otherPositions = new ObjectOpenHashSet<>();
    }

    public CasedBlockView setOtherAt(BlockPos pos) {
        otherPositions.add(pos);
        return this;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if (otherPositions.contains(pos)) {
            return otherState;
        }
        return super.getBlockState(pos);
    }

}