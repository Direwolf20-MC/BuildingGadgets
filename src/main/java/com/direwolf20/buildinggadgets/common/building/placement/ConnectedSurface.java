package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.Function;

/**
 * Surface that limits its attempt to blocks that are connected through either on its sides or corners. Candidates are
 * selected from a wall region centered at a point and filtered with a 8-way adjacent flood fill.
 *
 * @see #iterator()
 * @see Surface
 */
public final class ConnectedSurface implements IPlacementSequence {

    /**
     * @param world           Block access for searching reference
     * @param searchingCenter Center of the searching region
     * @param side            Facing to offset from the {@code searchingCenter} to get to the reference region center
     */
    public static ConnectedSurface create(IBlockAccess world, BlockPos searchingCenter, EnumFacing side, int range, boolean fuzzy) {
        Region searchingRegion = Wall.clickedSide(searchingCenter, side, range).getBoundingBox();
        return create(world, searchingRegion, pos -> pos.offset(side), searchingCenter, side, fuzzy);
    }

    public static ConnectedSurface create(IBlockAccess world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, EnumFacing side, boolean fuzzy) {
        return new ConnectedSurface(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
    }

    private final IBlockAccess world;
    private final Region searchingRegion;
    private final Function<BlockPos, BlockPos> searching2referenceMapper;
    private final BlockPos searchingCenter;
    private final EnumFacing side;
    private final boolean fuzzy;

    @VisibleForTesting
    private ConnectedSurface(IBlockAccess world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, EnumFacing side, boolean fuzzy) {
        this.world = world;
        this.searchingRegion = searchingRegion;
        this.searching2referenceMapper = searching2referenceMapper;
        this.searchingCenter = searchingCenter;
        this.side = side;
        this.fuzzy = fuzzy;
    }

    /**
     * The bounding box of the searchingRegion that is being searched.
     */
    @Override
    public Region getBoundingBox() {
        return searchingRegion;
    }

    /**
     * {@inheritDoc}<br>
     * <b>inaccurate representation (case 2)</b>:
     *
     * @return {@code getBoundingBox().contains(x, y, z)} since it would be costly to check each position
     */
    @Override
    public boolean mayContain(int x, int y, int z) {
        return searchingRegion.mayContain(x, y, z);
    }

    @Override
    public IPlacementSequence copy() {
        return new ConnectedSurface(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
    }

    /**
     * Uses a 8-way adjacent flood fill algorithm with Breadth-First Search to identify blocks with a valid path. A position
     * is valid if and only if it connects to the center and its underside block is the same as the underside of the center.
     *
     * @implNote Uses a 8-way adjacent flood fill algorithm with Breadth-First Search to identify blocks with a valid path.
     */
    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        IBlockState selectedBlock = getReferenceFor(searchingCenter);

        return new AbstractIterator<BlockPos>() {
            private Queue<BlockPos> queue = new ArrayDeque<>(searchingRegion.size());
            private ObjectSet<BlockPos> searched = new ObjectOpenHashSet<>();

            {
                queue.add(searchingCenter);
                searched.add(searchingCenter);
            }

            @Override
            protected BlockPos computeNext() {
                if (queue.isEmpty()) {
                    return endOfData();
                }

                // The position is guaranteed to be valid
                BlockPos current = queue.remove();

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        BlockPos neighbor = VectorTools.perpendicularSurfaceOffset(current, side, i, j);

                        boolean isSearched = !searched.add(neighbor);

                        if (isSearched || !searchingRegion.contains(neighbor) || !isStateValid(selectedBlock, neighbor))
                            continue;

                        queue.add(neighbor);
                    }
                }

                return current;
            }
        };
    }

    private boolean isStateValid(IBlockState filter, BlockPos pos) {
        IBlockState reference = getReferenceFor(pos);
        boolean isAir = reference.getBlock().isAir(reference, world, pos);
        // If fuzzy=true, we ignore the block for reference
        if (fuzzy)
            return !isAir;
        return !isAir && filter == reference;
    }

    private IBlockState getReferenceFor(BlockPos pos) {
        return world.getBlockState(searching2referenceMapper.apply(pos));
    }

}
