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

    /**
     * @deprecated ConnectedSurface should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new ConnectedSurface(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
    }

    /**
     * @implNote technically BFS fits better with a flood fill algorithm and it simpler to implement with a generator
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

                //The position is guaranteed to be valid
                BlockPos current = queue.remove();

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        BlockPos neighbor = VectorTools.perpendicularSurfaceOffset(current, side, i, j);

                        boolean isSearched = searched.contains(neighbor);
                        searched.add(neighbor);

                        if (isSearched || !searchingRegion.contains(neighbor) || !isStateValid(selectedBlock, neighbor)) {
                            continue;
                        }

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
        //If fuzzy=true, we ignore the block for reference
        if (fuzzy) {
            return !isAir;
        }
        return !isAir && filter == reference;
    }

    private IBlockState getReferenceFor(BlockPos pos) {
        return world.getBlockState(searching2referenceMapper.apply(pos));
    }

    //TODO use a bit set for iterator
    //  note: this should be handled by a custom class containing an origin, an axis to be ignored
    //  this should enable support for negative relative coordinates

//    @VisibleForTesting
//    private int toPlaner(BlockPos relative) {
//        Axis ignore = side.getAxis();
//        switch (ignore) {
//            case X:
//                return relative.getY() * searchingRegion.getZSize() + relative.getZ();
//            case Y:
//                return relative.getX() * searchingRegion.getZSize() + relative.getZ();
//            case Z:
//                return relative.getY() * searchingRegion.getXSize() + relative.getX();
//        }
//        throw new IllegalArgumentException();
//    }

}