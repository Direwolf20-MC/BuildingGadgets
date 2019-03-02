package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.Function;

public final class Surface implements IPlacementSequence {

    /**
     * @param world           Block access for searching reference
     * @param searchingCenter Center of the searching region
     * @param side            Facing to offset from the {@code searchingCenter} to get to the reference region center
     */
    public static Surface create(IBlockAccess world, BlockPos searchingCenter, EnumFacing side, int range, boolean fuzzy) {
        Region searchingRegion = Wall.clickedSide(searchingCenter, side, range).getBoundingBox();
        return create(world, searchingCenter, searchingRegion, pos -> pos.offset(side), fuzzy);
    }

    public static Surface create(IBlockAccess world, BlockPos center, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, boolean fuzzy) {
        return new Surface(world, center, searchingRegion, searching2referenceMapper, fuzzy);
    }

    private final IBlockAccess world;
    private final IBlockState selectedBase;
    private final Function<BlockPos, BlockPos> searching2referenceMapper;
    private final Region searchingRegion;
    private final boolean fuzzy;

    @VisibleForTesting
    private Surface(IBlockAccess world, BlockPos center, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, boolean fuzzy) {
        this.world = world;
        this.selectedBase = world.getBlockState(searching2referenceMapper.apply(center));
        this.searchingRegion = searchingRegion;
        this.searching2referenceMapper = searching2referenceMapper;
        this.fuzzy = fuzzy;
    }

    /**
     * For {@link #copy()}
     */
    private Surface(IBlockAccess world, IBlockState selectedBase, Function<BlockPos, BlockPos> searching2referenceMapper, Region searchingRegion, boolean fuzzy) {
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

    /**
     * @deprecated Surface should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new Surface(world, selectedBase, searching2referenceMapper, searchingRegion, fuzzy);
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
                    IBlockState baseBlock = world.getBlockState(referencePos);
                    if ((fuzzy || baseBlock == selectedBase) && !baseBlock.getBlock().isAir(baseBlock, world, referencePos)) {
                        return pos;
                    }
                }
                return endOfData();
            }
        };
    }

}
