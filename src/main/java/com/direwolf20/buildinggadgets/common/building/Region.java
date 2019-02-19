package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.building.placement.IPlacementSequence;
import com.google.common.base.MoreObjects;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a region in the world with a finite and nonzero size.
 * <p>Javadoc are copied from {@link AxisAlignedBB} with some modifications.</p>
 */
public final class Region implements IPlacementSequence, Comparable<Region> {

    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    public Region(int[] vertexes) {
        this(vertexes[0], vertexes[1], vertexes[2], vertexes[3], vertexes[4], vertexes[5]);
    }

    public Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public Region(Vec3i vertex) {
        this(vertex, vertex);
    }

    public Region(Vec3i minVertex, Vec3i maxVertex) {
        this(minVertex.getX(), minVertex.getY(), minVertex.getZ(), maxVertex.getX(), maxVertex.getY(), maxVertex.getZ());
    }

    /**
     * @return this
     */
    public Region offsetBy(int x, int y, int z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

    /**
     * @see #offsetBy(int, int, int)
     */
    public Region offsetBy(Vec3i direction) {
        return this.offsetBy(direction.getX(), direction.getY(), direction.getZ());
    }

    /**
     * Expand the current region by the given amount in both directions. Negative
     * values will shrink the region instead of expanding it.
     * <p>
     * Side lengths will be increased by 2 times the value of the parameters, since both min and max are changed.
     * </p>
     * <p>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid region - see last ample).
     * </p>
     *
     * <h3>Samples:</h3>
     * <table>
     * <tr><th>Input</th><th>Result</th></tr>
     * <tr><td><pre><code>new Region(0, 0, 0, 1, 1, 1).grow(2, 2, 2)</code></pre></td><td><pre><samp>box[-2, -2, -2 -> 3, 3, 3]</samp></pre></td></tr>
     * <tr><td><pre><code>new Region(0, 0, 0, 6, 6, 6).grow(-2, -2, -2)</code></pre></td><td><pre><samp>box[2, 2, 2 -> 4, 4, 4]</samp></pre></td></tr>
     * <tr><td><pre><code>new Region(5, 5, 5, 7, 7, 7).grow(0, 1, -1)</code></pre></td><td><pre><samp>box[5, 4, 6 -> 7, 8, 6]</samp></pre></td></tr>
     * <tr><td><pre><code>new Region(1, 1, 1, 3, 3, 3).grow(-4, -2, -3)</code></pre></td><td><pre><samp>box[-1, 1, 0 -> 5, 3, 4]</samp></pre></td></tr>
     * </table>
     *
     * <h3>See Also:</h3>
     * <ul>
     * <li>{@link #grow(int)} - version of this that expands in all directions from one parameter.</li>
     * <li>{@link #shrink(int)} - contracts in all directions</li>
     * </ul>
     *
     * @return this
     */
    public Region grow(int x, int y, int z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

    /**
     * Expand the current region by the given value in all directions. Equivalent to {@link
     * #grow(int, int, int)} with the given value for all 3 params. Negative values will shrink the region.
     * <br/>
     * Side lengths will be increased by 2 times the value of the parameter, since both min and max are changed.
     * <br/>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid region - see samples on {@link #grow(int, int, int)}).
     *
     * @return this
     */
    public Region grow(int size) {
        this.grow(size, size, size);
        return this;
    }

    /**
     * See {@link #grow(int, int, int)} subtracting instead of adding.
     *
     * @return this
     */
    public Region shrink(int x, int y, int z) {
        return this.grow(-x, -y, -z);
    }

    /**
     * See {@link #grow(int)} subtracting instead of adding.
     *
     * @return this
     */
    public Region shrink(int size) {
        return this.grow(-size);
    }

    /**
     * Create a new region with the intersecting part between the two regions.
     *
     * @return a new region
     */
    public Region intersect(Region other) {
        int minX = Math.max(this.minX, other.minX);
        int minY = Math.max(this.minY, other.minY);
        int minZ = Math.max(this.minZ, other.minZ);
        int maxX = Math.min(this.maxX, other.maxX);
        int maxY = Math.min(this.maxY, other.maxY);
        int maxZ = Math.min(this.maxZ, other.maxZ);
        return new Region(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean intersectsWith(Region other) {
        return this.maxX >= other.minX &&
                this.minX <= other.maxX &&
                this.maxZ >= other.minZ &&
                this.minZ <= other.maxZ &&
                this.maxY >= other.minY &&
                this.minY <= other.maxY;
    }

    /**
     * Create a new region that encloses both regions that has the minimum volume.
     *
     * @return a new region
     */
    public Region union(Region other) {
        int minX = Math.min(this.minX, other.minX);
        int minY = Math.min(this.minY, other.minY);
        int minZ = Math.min(this.minZ, other.minZ);
        int maxX = Math.max(this.maxX, other.maxX);
        int maxY = Math.max(this.maxY, other.maxY);
        int maxZ = Math.max(this.maxZ, other.maxZ);
        return new Region(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * @return this
     */
    @Override
    public Region getBoundingBox() {
        return this;
    }

    public StructureBoundingBox toStructureBoundingBox() {
        return new StructureBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean isXInBound(int x) {
        return x >= minX && x <= maxX;
    }

    public boolean isYInBound(int y) {
        return y >= minY && y <= maxY;
    }

    public boolean isZInBound(int z) {
        return z >= minZ && z <= maxZ;
    }

    public boolean isInBound(int x, int y, int z) {
        return isXInBound(x) && isYInBound(y) && isZInBound(z);
    }

    public boolean isInBound(Vec3i vec) {
        return isInBound(vec.getX(), vec.getY(), vec.getZ());
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

    public BlockPos getMinVec() {
        return new BlockPos(minX, minY, minZ);
    }

    public BlockPos getMaxVec() {
        return new BlockPos(maxX, maxY, maxZ);
    }

    public int getXSize() {
        return maxX - minX + 1;
    }

    public int getYSize() {
        return maxY - minY + 1;
    }

    public int getZSize() {
        return maxZ - minZ + 1;
    }

    public int getSize() {
        return getXSize() * getYSize() * getZSize();
    }

    /**
     * <p>
     * The first result will have the minimum x, y, and z value. In the process it will advance first in positive z, and then x, and then y direction.
     * In other words it will cover the first row with minimum x and y value, and then sweep through the lowest plane and work its way up.
     * </p>
     *
     * @return {@link AbstractIterator}
     * @implSpec starts at (minX, minY, minZ), ends at (maxX, maxY, maxZ)
     */
    @Override
    public Iterator<BlockPos> iterator() {
        return new AbstractIterator<BlockPos>() {
            private int posX = minX;
            private int posY = minY;
            private int posZ = minZ;

            @Override
            protected BlockPos computeNext() {
                if (this.isTerminated()) {
                    return endOfData();
                }

                if (!isXOverflowed()) {
                    posX++;
                } else if (!isZOverflowed()) {
                    posX = minX;
                    posZ++;
                } else if (!isYOverflowed()) {
                    posX = minX;
                    posZ = minZ;
                    posY++;
                }

                return new BlockPos(this.posX, this.posY, this.posZ);
            }

            private boolean isXOverflowed() {
                return posX == maxX;
            }

            private boolean isYOverflowed() {
                return posY == maxY;
            }

            private boolean isZOverflowed() {
                return posZ == maxZ;
            }

            private boolean isTerminated() {
                return isXOverflowed() && isYOverflowed() && isZOverflowed();
            }
        };
    }

    /**
     * Composes the result of the plain iterator with a custom value.
     *
     * @see #iterator()
     */
    public <T> Iterator<T> iterator(Predicate<BlockPos> validator, Function<BlockPos, T> composer) {
        Iterator<BlockPos> original = iterator();
        return new AbstractIterator<T>() {
            @Override
            protected T computeNext() {
                while (original.hasNext()) {
                    BlockPos pos = original.next();
                    if (validator.test(pos)) {
                        return composer.apply(pos);
                    }
                }
                return endOfData();
            }
        };
    }

    /**
     * @return a new Region with the exact same properties
     */
    public Region copy() {
        return new Region(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("minX", minX)
                .add("minY", minY)
                .add("minZ", minZ)
                .add("maxX", maxX)
                .add("maxY", maxY)
                .add("maxZ", maxZ)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region blockPos = (Region) o;
        return minX == blockPos.minX &&
                minY == blockPos.minY &&
                minZ == blockPos.minZ &&
                maxX == blockPos.maxX &&
                maxY == blockPos.maxY &&
                maxZ == blockPos.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int compareTo(Region that) {
        if (this.minX < that.minX) {
            return -1;
        } else if (this.minX > that.minX) {
            return 1;
        }

        if (this.minZ < that.minZ) {
            return -1;
        } else if (this.minZ > that.minZ) {
            return 1;
        }

        if (this.minY < that.minY) {
            return -1;
        } else if (this.minY > that.minY) {
            return 1;
        }

        if (this.maxX < that.maxX) {
            return -1;
        } else if (this.maxX > that.maxX) {
            return 1;
        }

        if (this.maxZ < that.maxZ) {
            return -1;
        } else if (this.maxZ > that.maxZ) {
            return 1;
        }

        if (this.maxY < that.maxY) {
            return -1;
        } else if (this.maxY > that.maxY) {
            return 1;
        }
        return 0;
    }

}
