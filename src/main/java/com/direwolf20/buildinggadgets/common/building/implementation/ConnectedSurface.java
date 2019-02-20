package com.direwolf20.buildinggadgets.common.building.implementation;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Queue;

public class ConnectedSurface implements IPlacementSequence {

    public static ConnectedSurface create(BlockPos posHit, EnumFacing sideHit, int range) {
        return new ConnectedSurface(posHit, sideHit, range);
    }

    private final Region region;
    private final BlockPos posHit;
    private final EnumFacing sideHit;

    protected ConnectedSurface(BlockPos posHit, EnumFacing sideHit, int range) {
        this.region = Wall.clickedSide(posHit, sideHit, range).getBoundingBox();
        this.posHit = posHit.toImmutable();
        this.sideHit = sideHit;
    }

    /**
     * For {@link #copy()}
     */
    private ConnectedSurface(Region region, BlockPos posHit, EnumFacing sideHit) {
        this.region = region;
        this.posHit = posHit;
        this.sideHit = sideHit;
    }

    @Override
    public Region getBoundingBox() {
        return region;
    }

    /**
     * {@inheritDoc}
     * @return {@code getBoundingBox().contains(x, y, z)} since it would be costly to check each position
     */
    @Override
    public boolean contains(int x, int y, int z) {
        return region.contains(x, y, z);
    }

    /**
     * @deprecated ConnectedSurface should be immutable, so this is not needed
     */
    @Deprecated
    @Override
    public IPlacementSequence copy() {
        return new ConnectedSurface(region, posHit, sideHit);
    }

    /**
     * @implNote technically BFS fits better with a flood fill algorithm and it simpler to implement with a generator
     */
    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private Queue<BlockPos> queue = new ArrayDeque<>(region.size());
            private BitSet visited = new BitSet();

            {
                queue.add(posHit);
                visited.set(toPlaner(posHit));
            }

            @Override
            protected BlockPos computeNext() {
                if (queue.isEmpty()) {
                    return endOfData();
                }

                BlockPos current = queue.remove();

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        BlockPos neighbor = VectorTools.perpendicularSurfaceOffset(current, sideHit, i, j);
                        int planer = toPlaner(neighbor);

                        if (visited.get(planer)) {
                            continue;
                        }
                        visited.set(planer);

                        if (region.contains(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }

                return current;
            }
        };
    }

    private int toPlaner(BlockPos pos) {
        BlockPos relative = pos.subtract(posHit);
        Axis ignore = sideHit.getAxis();
        switch (ignore) {
            case X:
                return relative.getY() * region.getZSize() + relative.getZ();
            case Y:
                return relative.getX() * region.getZSize() + relative.getZ();
            case Z:
                return relative.getY() * region.getXSize() + relative.getX();
        }
        throw new IllegalArgumentException();
    }

}
