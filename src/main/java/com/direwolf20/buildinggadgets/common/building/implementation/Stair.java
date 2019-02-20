package com.direwolf20.buildinggadgets.common.building.implementation;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPlacementSequence;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class Stair implements IPlacementSequence {

    public static Stair create(BlockPos base, EnumFacing horizontalAdvance, EnumFacing verticalAdvance, int range) {
        return new Stair(base, horizontalAdvance, verticalAdvance, range);
    }

    private final BlockPos base;
    private final BlockPos target;
    private final EnumFacing horizontalAdvance;
    private final EnumFacing verticalAdvance;
    private final Region region;
    private final int range;

    protected Stair(BlockPos base, EnumFacing horizontalAdvance, EnumFacing verticalAdvance, int range) {
        this.base = base;
        this.target = base.offset(horizontalAdvance, range).offset(verticalAdvance, range);
        this.horizontalAdvance = horizontalAdvance;
        this.verticalAdvance = verticalAdvance;
        this.region = new Region(base, target);
        this.range = range;
    }

    /**
     * For {@link #copy()}
     */
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
    public boolean contains(int x, int y, int z) {
        return region.contains(x, y, z);
    }

    /**
     * @deprecated Stair should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new Stair(base, target, horizontalAdvance, verticalAdvance, region, range);
    }

    @Override
    @Nonnull
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private int i;

            @Override
            protected BlockPos computeNext() {
                if (i >= range) {
                    return endOfData();
                }

                i++;
                return new BlockPos(base).offset(horizontalAdvance, i).offset(verticalAdvance, i);
            }
        };
    }

}
