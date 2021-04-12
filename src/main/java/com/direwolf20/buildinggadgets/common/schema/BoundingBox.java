package com.direwolf20.buildinggadgets.common.schema;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import java.util.Objects;
import java.util.stream.Stream;

public final class BoundingBox {
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

    public BoundingBox(Vector3i pos) {
        this(pos, pos);
    }

    public BoundingBox(Vector3i min, Vector3i max) {
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
        return BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ).map(BlockPos::immutable);
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
