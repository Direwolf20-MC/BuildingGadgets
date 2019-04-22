package com.direwolf20.buildinggadgets.api.building.placement;

import com.direwolf20.buildinggadgets.api.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * A sequence of blocks that offsets in 2 different directions where one is vertical, one is horizontal.
 * <p>
 * For example, a regular climbing up stair facing north would have (UP, NORTH) as its parameter. This also applies to
 * descending stair like (DOWN, SOUTH) where each block is lower than the latter.
 */
public final class Stair implements IPlacementSequence {

    public static Stair create(BlockPos base, EnumFacing horizontalAdvance, EnumFacing verticalAdvance, int range) {
        return new Stair(base, horizontalAdvance, verticalAdvance, range);
    }

    private final BlockPos base;
    private final BlockPos target;
    private final EnumFacing horizontalAdvance;
    private final EnumFacing verticalAdvance;
    private final Region region;
    private final int range;

    @VisibleForTesting
    private Stair(BlockPos base, EnumFacing horizontalAdvance, EnumFacing verticalAdvance, int range) {
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
    private Stair(BlockPos base, BlockPos target, EnumFacing horizontalAdvance, EnumFacing verticalAdvance, Region region, int range) {
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
    public IPlacementSequence copy() {
        return new Stair(base, target, horizontalAdvance, verticalAdvance, region, range);
    }

    @Override
    @Nonnull
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private MutableBlockPos current = new MutableBlockPos(base);
            private int i = 0;

            {
                current.move(horizontalAdvance, -1).move(verticalAdvance, -1);
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
