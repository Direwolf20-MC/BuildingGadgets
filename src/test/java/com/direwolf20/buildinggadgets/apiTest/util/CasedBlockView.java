package com.direwolf20.buildinggadgets.apiTest.util;

import com.direwolf20.buildinggadgets.api.building.Region;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * A fake world that has a finite set of positions filled with a certain type of block and others, which can be specified
 * with {@link #setOtherAt(BlockPos)}, to be another type of block.
 */
public class CasedBlockView extends RegionBlockView {

    public static final BlockState base = UniqueBlockState.createNew();
    public static final BlockState target = UniqueBlockState.createNew();

    private static final Random random = new Random();

    public static CasedBlockView regionAtOriginWithRandomTargets(int size, int amountTargets) {
        int radius = (size - 1) / 2;
        CasedBlockView world = new CasedBlockView(new Region(-radius, 0, -radius, radius, 0, radius), CasedBlockView.base, CasedBlockView.target);
        for (Iterator<Integer> it = random.ints(amountTargets, 0, size + 1).iterator(); it.hasNext(); ) {
            int x = it.next() - radius - 1;
            int z = it.next() - radius - 1;
            world.setOtherAt(new BlockPos(x, 0, z));
        }

        return world;
    }

    private BlockState otherState;
    private Set<BlockPos> otherPositions;

    public CasedBlockView(Region region, BlockState state, BlockState otherState) {
        super(region, state);
        this.otherState = otherState;
        this.otherPositions = new ObjectOpenHashSet<>();
    }

    public CasedBlockView setOtherAt(BlockPos pos) {
        otherPositions.add(pos);
        return this;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (otherPositions.contains(pos)) {
            return otherState;
        }
        return super.getBlockState(pos);
    }

}