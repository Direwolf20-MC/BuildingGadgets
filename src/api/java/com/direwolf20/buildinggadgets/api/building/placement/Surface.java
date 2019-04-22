package com.direwolf20.buildinggadgets.api.building.placement;

import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

/**
 * Surface mode where no connectivity is required. All blocks within the region (wall centered at some position) will
 * be selected if it fulfills the requirement -- has its underside same as the starting position.
 *
 * @see ConnectedSurface
 */
public final class Surface implements IPositionPlacementSequence {

    /**
     * @param world           Block access for searching reference
     * @param searchingCenter Center of the searching region
     * @param side            Facing to offset from the {@code searchingCenter} to get to the reference region center
     */
    public static Surface create(IBlockReader world, BlockPos searchingCenter, EnumFacing side, int range, boolean fuzzy) {
        Region searchingRegion = Wall.clickedSide(searchingCenter, side, range).getBoundingBox();
        return create(world, searchingCenter, searchingRegion, pos -> pos.offset(side), fuzzy);
    }

    public static Surface create(IBlockReader world, BlockPos center, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, boolean fuzzy) {
        return new Surface(Objects.requireNonNull(world,"Surface mode is required to have at least an IBlockReader"),
                           Objects.requireNonNull(center),
                           Objects.requireNonNull(searchingRegion),
                           Objects.requireNonNull(searching2referenceMapper),
                           fuzzy);
    }

    private final IBlockReader world;
    private final IBlockState selectedBase;
    private final Function<BlockPos, BlockPos> searching2referenceMapper;
    private final Region searchingRegion;
    private final boolean fuzzy;

    @VisibleForTesting
    private Surface(IBlockReader world, BlockPos center, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, boolean fuzzy) {
        this(world,world.getBlockState(searching2referenceMapper.apply(center)),searching2referenceMapper,searchingRegion,fuzzy);
    }

    /**
     * For {@link #copy()}
     */
    private Surface(IBlockReader world, IBlockState selectedBase, Function<BlockPos, BlockPos> searching2referenceMapper, Region searchingRegion, boolean fuzzy) {
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
                    if ((fuzzy || baseBlock == selectedBase) && !baseBlock.getBlock().isAir(baseBlock, world, referencePos))
                        return pos;
                }
                return endOfData();
            }
        };
    }

}
