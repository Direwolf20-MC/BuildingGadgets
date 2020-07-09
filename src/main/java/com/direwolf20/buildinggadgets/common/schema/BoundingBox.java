package com.direwolf20.buildinggadgets.common.schema;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.Vec3i;

import java.util.Objects;
import java.util.stream.Stream;

public class BoundingBox {
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;

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
     * @param x x-growth
     * @param y y-growth
     * @param z z-growth
     * @return A new Region who's max coordinates are increased by the given amount
     */
    public BoundingBox grow(int x, int y, int z) {
        return new BoundingBox(minX, minY, minZ, maxX + x, maxY + y, maxZ + z);
    }

    /**
     * Supplied by MC which this class is an extension of.
     *
     * @return MC designed MutableBoundingBox
     */
    public MutableBoundingBox toMutableBoundingBox() {
        return new MutableBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Stream<BlockPos> getBlocksPosWithin() {
        return BlockPos.getAllInBox(this.toMutableBoundingBox());
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
