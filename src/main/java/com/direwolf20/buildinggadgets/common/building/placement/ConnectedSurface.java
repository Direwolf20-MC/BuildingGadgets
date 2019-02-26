package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public final class ConnectedSurface implements IPlacementSequence {

    /**
     * @param world  Block access for searching reference
     * @param center Center of the region for reference
     * @param side   Facing to offset from the {@code center} to get to the region to search
     */
    public static ConnectedSurface create(IBlockAccess world, BlockPos center, EnumFacing side, int range, boolean fuzzy) {
        return new ConnectedSurface(world, center, side, range, fuzzy);
    }

    private final IBlockAccess world;
    private final Region region;
    private final BlockPos center;
    private final EnumFacing side;
    private final boolean fuzzy;

    @VisibleForTesting
    private ConnectedSurface(IBlockAccess world, BlockPos center, EnumFacing side, int range, boolean fuzzy) {
        this.world = world;
        this.region = Wall.clickedSide(center.offset(side), side, range).getBoundingBox();
        this.center = center.toImmutable();
        this.side = side;
        this.fuzzy = fuzzy;
    }

    /**
     * For {@link #copy()}
     */
    @VisibleForTesting
    private ConnectedSurface(IBlockAccess world, Region region, BlockPos center, EnumFacing side, boolean fuzzy) {
        this.world = world;
        this.region = region;
        this.center = center;
        this.side = side;
        this.fuzzy = fuzzy;
    }

    /**
     * The bounding box of the region that is being searched.
     */
    @Override
    public Region getBoundingBox() {
        return region;
    }

    /**
     * {@inheritDoc}<br>
     * <b>inaccurate representation (case 2)</b>:
     *
     * @return {@code getBoundingBox().contains(x, y, z)} since it would be costly to check each position
     */
    @Override
    public boolean mayContain(int x, int y, int z) {
        return region.mayContain(x, y, z);
    }

    /**
     * @deprecated ConnectedSurface should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new ConnectedSurface(world, region, center, side, fuzzy);
    }

    /**
     * @implNote technically BFS fits better with a flood fill algorithm and it simpler to implement with a generator
     */
    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        IBlockState selectedBlock = world.getBlockState(center);
        BlockPos searchCenter = center.offset(side);

        return new AbstractIterator<BlockPos>() {
            private Queue<BlockPos> queue = new ArrayDeque<>(region.size());
            private ObjectSet<BlockPos> searched = new ObjectOpenHashSet<>();

            {
                queue.add(searchCenter);
                searched.add(searchCenter);
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

                        //If fuzzy=true, we ignore the block for reference
                        if (isSearched || !region.contains(neighbor) || !isStateValid(selectedBlock, getReferenceFor(neighbor))) {
                            continue;
                        }

                        queue.add(neighbor);
                    }
                }

                return current;
            }
        };
    }

    private boolean isStateValid(IBlockState filter, IBlockState reference) {
        boolean isAir = reference.getMaterial() == Material.AIR;
        if (fuzzy) {
            return !isAir;
        }
        return !isAir && filter == reference;
    }

    private IBlockState getReferenceFor(BlockPos searchPos) {
        return world.getBlockState(searchPos.offset(side.getOpposite()));
    }

    //TODO use a bit set for iterator
    //  note: this should be handled by a custom class containing an origin, an axis to be ignored
    //  this should enable support for negative relative coordinates

//    @VisibleForTesting
//    private int toPlaner(BlockPos relative) {
//        Axis ignore = side.getAxis();
//        switch (ignore) {
//            case X:
//                return relative.getY() * region.getZSize() + relative.getZ();
//            case Y:
//                return relative.getX() * region.getZSize() + relative.getZ();
//            case Z:
//                return relative.getY() * region.getXSize() + relative.getX();
//        }
//        throw new IllegalArgumentException();
//    }

}
