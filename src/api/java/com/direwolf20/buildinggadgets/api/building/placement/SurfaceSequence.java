package com.direwolf20.buildinggadgets.api.building.placement;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.Function;

final class SurfaceSequence implements IPositionPlacementSequence {

    private final IBlockReader world;
    private final BlockState selectedBase;
    private final Function<BlockPos, BlockPos> searching2referenceMapper;
    private final Region searchingRegion;
    private final boolean fuzzy;

    @VisibleForTesting
    SurfaceSequence(IBlockReader world, BlockPos center, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, boolean fuzzy) {
        this(world, world.getBlockState(searching2referenceMapper.apply(center)), searching2referenceMapper, searchingRegion, fuzzy);
    }

    /**
     * For {@link #copy()}
     */
    SurfaceSequence(IBlockReader world, BlockState selectedBase, Function<BlockPos, BlockPos> searching2referenceMapper, Region searchingRegion, boolean fuzzy) {
        this.world = world;
        this.selectedBase = selectedBase;
        this.searching2referenceMapper = searching2referenceMapper;
        this.searchingRegion = searchingRegion;
        this.fuzzy = fuzzy;
    }

    @Override
    public Region getBoundingBox() {
        return searchingRegion;
    }

    /**
     * {@inheritDoc}<br>
     * <b>inaccurate representation (case 2)</b>:
     */
    @Override
    public boolean mayContain(int x, int y, int z) {
        return searchingRegion.contains(x, y, z);
    }

    @Override
    public IPositionPlacementSequence copy() {
        return new SurfaceSequence(world, selectedBase, searching2referenceMapper, searchingRegion, fuzzy);
    }

    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private final Iterator<BlockPos> it = searchingRegion.iterator();

            @Override
            protected BlockPos computeNext() {
                while (it.hasNext()) {
                    BlockPos pos = it.next();
                    BlockPos referencePos = searching2referenceMapper.apply(pos);
                    BlockState baseBlock = world.getBlockState(referencePos);
                    if ((fuzzy || baseBlock == selectedBase) && ! baseBlock.getBlock().isAir(baseBlock, world, referencePos))
                        return pos;
                }
                return endOfData();
            }
        };
    }
}
