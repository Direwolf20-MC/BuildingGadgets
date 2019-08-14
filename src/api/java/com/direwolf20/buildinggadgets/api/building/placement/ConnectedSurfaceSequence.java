package com.direwolf20.buildinggadgets.api.building.placement;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.util.VectorUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

final class ConnectedSurfaceSequence implements IPositionPlacementSequence {
    private final IBlockReader world;
    private final Region searchingRegion;
    private final Function<BlockPos, BlockPos> searching2referenceMapper;
    private final BlockPos searchingCenter;
    @Nullable
    private final Direction side;
    private final boolean fuzzy;

    @VisibleForTesting
    ConnectedSurfaceSequence(IBlockReader world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, boolean fuzzy) {
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
    public IPositionPlacementSequence copy() {
        return new ConnectedSurfaceSequence(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
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
        BlockState selectedBlock = getReferenceFor(searchingCenter);

        return new AbstractIterator<BlockPos>() {
            private Queue<BlockPos> queue = new LinkedList<>();
            private Set<BlockPos> searched = new HashSet<>(searchingRegion.size());

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

                for (int i = - 1; i <= 1; i++) {
                    for (int j = - 1; j <= 1; j++) {
                        if (side != null) {
                            BlockPos neighbor = VectorUtils.perpendicularSurfaceOffset(current, side, i, j);
                            addNeighbour(neighbor);
                        } else {
                            for (int k = - 1; k <= 1; k++) {
                                BlockPos neighbor = current.add(i, j, k);
                                addNeighbour(neighbor);
                            }
                        }
                    }
                }

                return current;
            }

            private void addNeighbour(BlockPos neighbor) {
                boolean isSearched = ! searched.add(neighbor);
                if (isSearched || ! searchingRegion.contains(neighbor) || ! isStateValid(selectedBlock, neighbor))
                    return;
                queue.add(neighbor);
            }
        };
    }

    private boolean isStateValid(BlockState filter, BlockPos pos) {
        BlockState reference = getReferenceFor(pos);
        boolean isAir = reference.isAir(world, pos);
        // If fuzzy=true, we ignore the block for reference
        return ! isAir && (fuzzy || filter == reference);
    }

    private BlockState getReferenceFor(BlockPos pos) {
        return world.getBlockState(searching2referenceMapper.apply(pos));
    }
}
