package com.direwolf20.buildinggadgets.common.tainted.building.placement;

import com.direwolf20.buildinggadgets.common.tainted.building.Region;
import com.google.common.collect.AbstractIterator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class ConnectedSurface implements Iterable<BlockPos> {
    public static ConnectedSurface create(Region searchingRegion, BlockGetter world, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
        return create(searchingRegion, world, pos -> pos.relative(side), searchingCenter, side, range, fuzzy);
    }

    public static ConnectedSurface create(Region searchingRegion, BlockGetter world, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
        return create(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
    }

    public static ConnectedSurface create(BlockGetter world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, boolean fuzzy) {
        return new ConnectedSurface(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
    }

    public static ConnectedSurface create(BlockGetter world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, BiPredicate<BlockState, BlockPos> predicate) {
        return new ConnectedSurface(world, searchingRegion, searching2referenceMapper, searchingCenter, side, predicate);
    }

    private final BlockGetter world;
    private final Region searchingRegion;
    private final Function<BlockPos, BlockPos> searching2referenceMapper;
    private final BlockPos searchingCenter;

    @Nullable
    private final Direction side;
    private final BiPredicate<BlockState, BlockPos> predicate;

    ConnectedSurface(BlockGetter world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, boolean fuzzy) {
        this(world, searchingRegion, searching2referenceMapper, searchingCenter, side,
                (filter, pos) -> {
                    BlockState reference = world.getBlockState(searching2referenceMapper.apply(pos));
                    boolean isAir = reference.isAir(world, pos);
                    // If fuzzy=true, we ignore the block for reference
                    return ! isAir && (fuzzy || filter == reference);
                });
    }

    ConnectedSurface(BlockGetter world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, BiPredicate<BlockState, BlockPos> predicate) {
        this.world = world;
        this.searchingRegion = searchingRegion;
        this.searching2referenceMapper = searching2referenceMapper;
        this.searchingCenter = searchingCenter;
        this.side = side;
        this.predicate = predicate;
    }

    /**
     * The bounding box of the searchingRegion that is being searched.
     */
    public Region getBoundingBox() {
        return searchingRegion;
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
                if (isValid(searchingCenter)) { //The destruction Gadget might be facing Bedrock or something similar - this would not be valid!
                    queue.add(searchingCenter);
                    searched.add(searchingCenter);
                }
            }

            @Override
            protected BlockPos computeNext() {
                if (queue.isEmpty())
                    return endOfData();

                // The position is guaranteed to be valid
                BlockPos current = queue.remove();

                for (int i = - 1; i <= 1; i++) {
                    for (int j = - 1; j <= 1; j++) {
                        if (side != null) {
                            BlockPos neighbor = perpendicularSurfaceOffset(current, side.getAxis(), i, j);
                            addNeighbour(neighbor);
                        } else {
                            for (int k = - 1; k <= 1; k++) {
                                BlockPos neighbor = current.offset(i, j, k);
                                addNeighbour(neighbor);
                            }
                        }
                    }
                }
                return current;
            }

            private void addNeighbour(BlockPos neighbor) {
                boolean isSearched = ! searched.add(neighbor);
                if (isSearched || ! isValid(neighbor))
                    return;
                queue.add(neighbor);
            }

            private boolean isValid(BlockPos pos) {
                return searchingRegion.contains(pos) && predicate.test(selectedBlock, pos);
            }
        };
    }

    private BlockState getReferenceFor(BlockPos pos) {
        return world.getBlockState(searching2referenceMapper.apply(pos));
    }

    public BlockPos perpendicularSurfaceOffset(BlockPos pos, Direction.Axis intersector, int i, int j) {
        switch (intersector) {
            case X:
                return pos.offset(0, i, j);
            case Y:
                return pos.offset(i, 0, j);
            case Z:
                return pos.offset(i, j, 0);
        }
        throw new IllegalArgumentException("Unknown facing " + intersector);
    }
}

