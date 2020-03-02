package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

final class StairSequence implements IPositionPlacementSequence {
    private final BlockPos base;
    private final BlockPos target;
    private final Direction horizontalAdvance;
    private final Direction verticalAdvance;
    private final Region region;
    private final int range;

    @VisibleForTesting
    StairSequence(BlockPos base, Direction horizontalAdvance, Direction verticalAdvance, int range) {
        this.base = base;
        this.target = base.offset(horizontalAdvance, range - 1).offset(verticalAdvance, range - 1);
        this.horizontalAdvance = horizontalAdvance;
        this.verticalAdvance = verticalAdvance;
        this.region = new Region(base, target);
        this.range = range;
    }

    /**
     * For {@link #copy()}
     */
    @VisibleForTesting
    private StairSequence(BlockPos base, BlockPos target, Direction horizontalAdvance, Direction verticalAdvance, Region region, int range) {
        this.base = base;
        this.target = target;
        this.horizontalAdvance = horizontalAdvance;
        this.verticalAdvance = verticalAdvance;
        this.region = region;
        this.range = range;
    }

    @Override
    public Region getBoundingBox() {
        return region;
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return region.mayContain(x, y, z);
    }

    @Override
    public IPositionPlacementSequence copy() {
        return new StairSequence(base, target, horizontalAdvance, verticalAdvance, region, range);
    }

    @Override
    @Nonnull
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private BlockPos.Mutable current = new BlockPos.Mutable(base);
            private int i = 0;

            {
                current.move(horizontalAdvance, - 1).move(verticalAdvance, - 1);
            }

            @Override
            protected BlockPos computeNext() {
                if (i >= range)
                    return endOfData();
                i++;

                current.move(horizontalAdvance, 1).move(verticalAdvance, 1);
                return current.toImmutable();
            }
        };
    }
}
