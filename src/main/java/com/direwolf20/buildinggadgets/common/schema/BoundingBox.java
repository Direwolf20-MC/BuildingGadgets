package com.direwolf20.buildinggadgets.common.schema;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3i;

import java.util.Objects;
import java.util.stream.Stream;

public final class BoundingBox {
    public static BoundingBox of(int... array) {
        Preconditions.checkArgument(array.length == 6);
        return new BoundingBox(array[0], array[1], array[2], array[3], array[4], array[5]);
    }

    public static final BoundingBox ZEROS = new BoundingBox(BlockPos.ZERO, BlockPos.ZERO);
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public BoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public BoundingBox(Vec3i pos) {
        this(pos, pos);
    }

    public BoundingBox(Vec3i min, Vec3i max) {
        this(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public BoundingBox expand(int x, int y, int z) {
        return new BoundingBox(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z);
    }

    /**
     * @return A new Region who's max coordinates are increased by the given amount
     */
    public BoundingBox grow(int x, int y, int z) {
        return new BoundingBox(minX, minY, minZ, maxX + x, maxY + y, maxZ + z);
    }

    public Stream<BlockPos> stream() {
        return BlockPos.getAllInBox(minX, minY, minZ, maxX, maxY, maxZ).map(BlockPos::toImmutable);
    }

    public Iterable<Mutable> yxzIterable() {
        return () -> new AbstractIterator<Mutable>() {
            private final Mutable pos = new Mutable(minX, minY, minZ);

            @Override
            protected Mutable computeNext() {
                int newY = pos.getY() + 1;
                int newX = pos.getX();
                int newZ = pos.getZ();
                if (newY > maxY) {
                    newY = minY;
                    if (++ newX > maxX && ++ newZ <= maxZ)
                        newX = minX;
                    else if (newZ > maxZ)
                        return endOfData();
                }
                return pos.setPos(newX, newY, newZ);
            }
        };
    }

    public BlockPos createMinPos() {
        return new BlockPos(getMinX(), getMinY(), getMinZ());
    }

    public BlockPos createMaxPos() {
        return new BlockPos(getMaxX(), getMaxY(), getMaxZ());
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int[] toArray() {
        return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoundingBox that = (BoundingBox) o;
        return minX == that.minX &&
                minY == that.minY &&
                minZ == that.minZ &&
                maxX == that.maxX &&
                maxY == that.maxY &&
                maxZ == that.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "minX=" + minX +
                ", minY=" + minY +
                ", minZ=" + minZ +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                ", maxZ=" + maxZ +
                '}';
    }
}
