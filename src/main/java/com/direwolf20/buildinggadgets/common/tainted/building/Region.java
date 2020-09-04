package com.direwolf20.buildinggadgets.common.tainted.building;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a region in the world with a finite and nonzero size.
 */
public final class Region implements Serializable {
    private static final Region ZERO = new Region(BlockPos.NULL_VECTOR);

    public static Region singleZero() {
        return ZERO;
    }

    /**
     * Creates a new {@link Builder} which initially contains {@code (0, 0, 0) to (0, 0, 0)}.
     *
     * @return A new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new {@link Builder} which initially only contains {@code (Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE) to (Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE)}.
     * <br>
     * Useful for creating a builder which encloses all Positions passed in.
     *
     * @return A new {@link Builder}
     */
    public static Builder enclosingBuilder() {
        return enclosingBuilder(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public static Builder enclosingBuilder(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return new Builder(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static final long serialVersionUID = 8391481277782374853L;

    public static Region deserializeFrom(CompoundNBT tag) {
        return new Region(
                tag.getInt(NBTKeys.KEY_MIN_X),
                tag.getInt(NBTKeys.KEY_MIN_Y),
                tag.getInt(NBTKeys.KEY_MIN_Z),
                tag.getInt(NBTKeys.KEY_MAX_X),
                tag.getInt(NBTKeys.KEY_MAX_Y),
                tag.getInt(NBTKeys.KEY_MAX_Z)
        );
    }

    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    public Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public Region(Vector3i vertex) {
        this(vertex, vertex);
    }

    public Region(Vector3i min, Vector3i max) {
        this(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    /**
     * Translates this Region by the given amount
     *
     * @param x how much to translate x
     * @param y how much to translate y
     * @param z how much to translate z
     * @return A new Region, translated by the given coordinates
     */
    public Region translate(int x, int y, int z) {
        return new Region(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }

    /**
     * @see #translate(int, int, int)
     *
     * @param direction Direction to translate to
     * @return {@link Region}
     */
    public Region translate(Vector3i direction) {
        return this.translate(direction.getX(), direction.getY(), direction.getZ());
    }

    public Region inverseTranslate(Vector3i direction) {
        return this.translate(- direction.getX(), - direction.getY(), - direction.getZ());
    }

    /**
     * @param x x-growth
     * @param y y-growth
     * @param z z-growth
     * @return A new Region who's max coordinates are increased by the given amount
     */
    public Region grow(int x, int y, int z) {
        return new Region(minX, minY, minZ, maxX + x, maxY + y, maxZ + z);
    }

    /**
     * See {@link #grow(int, int, int)} - grown on all three axes.
     *
     * @param size Size to grow to
     * @return {@link Region}
     */
    public Region grow(int size) {
        return this.grow(size, size, size);
    }

    /**
     * See {@link #grow(int, int, int)} - subtracting instead of adding.
     *
     * @param x X
     * @param y Y
     * @param z Z
     *
     * @return {@link Region}
     */
    public Region shrink(int x, int y, int z) {
        return this.grow(-x, -y, -z);
    }

    /**
     * See {@link #grow(int)} - subtracting instead of adding.
     * @param size Size to shrink to
     * @return {@link Region}
     */
    public Region shrink(int size) {
        return this.grow(-size);
    }

    /**
     * Expand the current region by the given amount in both directions. Negative
     * values will shrink the region instead of expanding it.
     * <p>
     * Side lengths will be increased by 2 times the value of the parameters, since both min and max are changed.
     * <p>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid region - see last sample).
     *
     * <h3>Samples:</h3>
     * <table summary="A few examples of Regions">
     * <tr><th>Input</th><th>Result</th></tr>
     * <tr><td><pre>{new Region(0, 0, 0, 1, 1, 1).grow(2, 2, 2)}</pre></td><td><pre><code>{box[-2, -2, -2 to 3, 3, 3]}</code></pre></td></tr>
     * <tr><td><pre>{new Region(0, 0, 0, 6, 6, 6).grow(-2, -2, -2)}</pre></td><td><pre><code>{box[2, 2, 2 to 4, 4, 4]}</code></pre></td></tr>
     * <tr><td><pre>{new Region(5, 5, 5, 7, 7, 7).grow(0, 1, -1)}</pre></td><td><pre><code>{box[5, 4, 6 to 7, 8, 6]}</code></pre></td></tr>
     * <tr><td><pre>{new Region(1, 1, 1, 3, 3, 3).grow(-4, -2, -3)}</pre></td><td><pre><code>{box[-1, 1, 0 to 5, 3, 4]}</code></pre></td></tr>
     * </table>
     *
     * <h3>See Also:</h3>
     * <ul>
     * <li>{@link #grow(int)} - version of this that expands in all directions from one parameter.
     * <li>{@link #shrink(int)} - contracts in all directions
     * </ul>
     *
     *
     * @param x X
     * @param y Y
     * @param z Z
     * @return {@link Region}
     */
    public Region expand(int x, int y, int z) {
        return new Region(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z);
    }

    public Region expand(Vector3i vec) {
        return expand(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Expand the current region by the given value in the max values. Equivalent to   with the given value for all 3 params. Negative values will shrink the region.
     * <p>
     * Side lengths will be increased by 2 times the value of the parameter, since both min and max are changed.
     * <p>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid region - see samples on {@link #grow(int, int, int)}).
     *
     *
     * @param size Size to expand to
     * @return {@link Region}
     */
    public Region expand(int size) {
        return expand(size, size, size);
    }

    /**
     * @param x X
     * @param y Y
     * @param z Z
     * @see #expand(int, int, int) - substracting instead of adding.
     * @return {@link Region}
     */
    public Region collapse(int x, int y, int z) {
        return expand(-x, -y, -z);
    }

    /**
     * @see #collapse(int, int, int) - read x, y, and z from the {@link Vector3i}.
     * @param vec Vector3i to collapse to
     * @return {@link Region}
     */
    public Region collapse(Vector3i vec) {
        return collapse(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * @see #collapse(int, int, int) - collapse on all three axes.
     *
     * @param size Size to collapse to
     * @return {@link Region}
     */
    public Region collapse(int size) {
        return expand(-size);
    }

    /**
     * Create a new region with the intersecting part between the two regions.
     *
     * @param other another region
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

    /**
     * Create a new region that encloses both regions
     *
     * @param other another region
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

    public BlockPos getMin() {
        return new BlockPos(minX, minY, minZ);
    }

    public BlockPos getMax() {
        return new BlockPos(maxX, maxY, maxZ);
    }

    public int getXSize() {
        return Math.abs(maxX - minX) + 1;
    }

    public int getYSize() {
        return Math.abs(maxY - minY) + 1;
    }

    public int getZSize() {
        return Math.abs(maxZ - minZ) + 1;
    }

    public int size() {
        return getXSize() * getYSize() * getZSize();
    }

    public boolean containsX(int x) {
        return x >= minX && x <= maxX;
    }

    public boolean containsY(int y) {
        return y >= minY && y <= maxY;
    }

    public boolean containsZ(int z) {
        return z >= minZ && z <= maxZ;
    }

//    /**
//     * Accurate representation of whether the position is a part the structure or not.
//     *
//     * @see #contains(int, int, int)
//     */
//    @Override
//    public boolean mayContain(int x, int y, int z) {
//        return contains(x, y, z);
//    }

    /**
     * @param x X
     * @param y Y
     * @param z Z
     *
     * @return whether or not this {@link BlockPos} lies within this Region
     */
    public boolean contains(int x, int y, int z) {
        return containsX(x) && containsY(y) && containsZ(z);
    }

    public boolean intersectsWith(Region other) {
        return this.maxX >= other.minX &&
                this.minX <= other.maxX &&
                this.maxZ >= other.minZ &&
                this.minZ <= other.maxZ &&
                this.maxY >= other.minY &&
                this.minY <= other.maxY;
    }

    public Stream<BlockPos> stream() {
        return BlockPos.getAllInBox(minX, minY, minZ, maxX, maxY, maxZ).map(BlockPos::toImmutable);
    }

//    /**
//     * The first result will have the minimum x, y, and z value. In the process it will advance in positive z-y-x order as used in BG-Code on various other places.
//     * Positions provided by this Iterator may be considered ordered.
//     *
//     * @return A {@link PeekingIterator} over all positions in this Region
//     * @implSpec starts at (minX, minY, minZ), ends at (maxX, maxY, maxZ)
//     * @implNote the Iterator does not support removal Operations
//     * @see com.direwolf20.buildinggadgets.common.util.CommonUtils#POSITION_COMPARATOR
//     */
//    @Override
//    public PeekingIterator<BlockPos> iterator() {
//        return new SpliteratorBackedPeekingIterator<>(spliterator());
//    }

//    /**
//     * Creates a {@link Spliterator} over the positions described by this {@code Region}.
//     *
//     * @return a {@link Spliterator} over the positions described by this {@code Region}.
//     * @implSpec The returned {@link Spliterator} will be Immutable, Sorted and Sized
//     */
//    @Override
//    public Spliterator<BlockPos> spliterator() {
//        return new RegionSpliterator(this);
//    }

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
        Region region = (Region) o;
        return minX == region.minX &&
                minY == region.minY &&
                minZ == region.minZ &&
                maxX == region.maxX &&
                maxY == region.maxY &&
                maxZ == region.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @SuppressWarnings("deprecation")
    public ImmutableSortedSet<ChunkPos> getUnloadedChunks(IWorldReader reader) {
        ImmutableSortedSet.Builder<ChunkPos> posBuilder = ImmutableSortedSet.orderedBy(Comparator.comparing(ChunkPos::getXStart).thenComparing(ChunkPos::getZStart));
        for (int i = minX; i <= maxX; i += 16) {
            for (int j = minZ; j <= maxZ; j += 16) {
                if (! reader.chunkExists(i >> 4, j >> 4))
                    posBuilder.add(new ChunkPos(i >> 4, j >> 4));
            }
        }
        for (int j = minZ; j <= maxZ; j += 16) {//check the last x row
            if (! reader.chunkExists(maxX >> 4, j >> 4))
                posBuilder.add(new ChunkPos(maxX >> 4, j >> 4));
        }
        if (! reader.chunkExists(maxX >> 4, maxZ >> 4))// might have still missed the last one
            posBuilder.add(new ChunkPos(maxX >> 4, maxZ >> 4));
        return posBuilder.build();
    }

    public CompoundNBT serialize() {
        return serializeTo(new CompoundNBT());
    }

    public CompoundNBT serializeTo(CompoundNBT tag) {
        tag.putInt(NBTKeys.KEY_MIN_X, minX);
        tag.putInt(NBTKeys.KEY_MIN_Y, minY);
        tag.putInt(NBTKeys.KEY_MIN_Z, minZ);
        tag.putInt(NBTKeys.KEY_MAX_X, maxX);
        tag.putInt(NBTKeys.KEY_MAX_Y, maxY);
        tag.putInt(NBTKeys.KEY_MAX_Z, maxZ);
        return tag;
    }

    public static class Builder {
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;

        private Builder() {
            this(0, 0, 0, 0, 0, 0);
        }

        public Builder(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        /**
         * Allows enclosing of all Vectors in the passed iterable.
         *
         * @param iterable The iterable who's contents shall be in the resulting Region
         * @return The {@code Builder} to allow for Method chaining
         * @see #enclose(Vector3i)
         */
        public Builder encloseAll(Iterable<? extends Vector3i> iterable) {
            for (Vector3i vec : iterable) {
                enclose(vec);
            }
            return this;
        }

        public Builder enclose(Region region) {
            enclose(region.getMin());
            enclose(region.getMax());
            return this;
        }

        /**
         * @param vec Vex3i
         * @see #enclose(int, int, int)
         *
         * @return {@link Builder}
         */
        public Builder enclose(Vector3i vec) {
            return enclose(vec.getX(), vec.getY(), vec.getZ());
        }

        /**
         * Ensures that the passed in coordinates are included in the resulting {@link Region}.
         *
         * @param x the x Coordinate which has to be included in the resulting {@link Region}
         * @param y the y Coordinate which has to be included in the resulting {@link Region}
         * @param z the z Coordinate which has to be included in the resulting {@link Region}
         * @return The {@code Builder} to allow for Method chaining
         * @see #encloseX(int)
         * @see #encloseY(int)
         * @see #encloseZ(int)
         */
        public Builder enclose(int x, int y, int z) {
            encloseX(x);
            encloseY(y);
            encloseZ(z);
            return this;
        }

        /**
         * Ensures that the passed in x Coordinate is included in the resulting {@link Region}.
         *
         * @param x the x Coordinate which has to be included in the resulting {@link Region}
         * @return The {@code Builder} to allow for Method chaining
         */
        public Builder encloseX(int x) {
            minX = Math.min(x, minX);
            maxX = Math.max(x, maxX);
            return this;
        }

        /**
         * Ensures that the passed in y Coordinate is included in the resulting {@link Region}.
         *
         * @param y the y Coordinate which has to be included in the resulting {@link Region}
         * @return The {@code Builder} to allow for Method chaining
         */
        public Builder encloseY(int y) {
            minY = Math.min(y, minY);
            maxY = Math.max(y, maxY);
            return this;
        }

        /**
         * Ensures that the passed in y Coordinate is included in the resulting {@link Region}.
         *
         * @param z the z Coordinate which has to be included in the resulting {@link Region}
         * @return The {@code Builder} to allow for Method chaining
         */
        public Builder encloseZ(int z) {
            minZ = Math.min(z, minZ);
            maxZ = Math.max(z, maxZ);
            return this;
        }

        public Region build() {
            return new Region(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}