package com.direwolf20.buildinggadgets.api.building;

import com.direwolf20.buildinggadgets.api.util.CommonUtils;
import com.direwolf20.buildinggadgets.api.util.SpliteratorBackedPeekingIterator;
import com.google.common.base.MoreObjects;
import com.google.common.collect.PeekingIterator;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Represents a region in the world with a finite and nonzero size.
 */
public final class Region implements IPositionPlacementSequence, Serializable {

    private static final long serialVersionUID = 8391481277782374853L;
    public static final String KEY_MIN_X = "minX";
    public static final String KEY_MIN_Y = "minY";
    public static final String KEY_MIN_Z = "minZ";
    public static final String KEY_MAX_X = "maxX";
    public static final String KEY_MAX_Y = "maxY";
    public static final String KEY_MAX_Z = "maxZ";

    public static Region deserializeFrom(CompoundNBT tag) {
        return new Region(
                tag.getInt(KEY_MIN_X),
                tag.getInt(KEY_MIN_Y),
                tag.getInt(KEY_MIN_Z),
                tag.getInt(KEY_MAX_X),
                tag.getInt(KEY_MAX_Y),
                tag.getInt(KEY_MAX_Z)
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

    public Region(Vec3i vertex) {
        this(vertex, vertex);
    }

    public Region(Vec3i min, Vec3i max) {
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
     */
    public Region translate(Vec3i direction) {
        return this.translate(direction.getX(), direction.getY(), direction.getZ());
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
     */
    public Region grow(int size) {
        return this.grow(size, size, size);
    }

    /**
     * See {@link #grow(int, int, int)} - subtracting instead of adding.
     */
    public Region shrink(int x, int y, int z) {
        return this.grow(-x, -y, -z);
    }

    /**
     * See {@link #grow(int)} - subtracting instead of adding.
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
     * <table>
     * <tr><th>Input</th><th>Result</th></tr>
     * <tr><td><pre>{@code new Region(0, 0, 0, 1, 1, 1).grow(2, 2, 2)}</pre></td><td><pre><samp>{@code box[-2, -2, -2 -> 3, 3, 3]}</samp></pre></td></tr>
     * <tr><td><pre>{@code new Region(0, 0, 0, 6, 6, 6).grow(-2, -2, -2)}</pre></td><td><pre><samp>{@code box[2, 2, 2 -> 4, 4, 4]}</samp></pre></td></tr>
     * <tr><td><pre>{@code new Region(5, 5, 5, 7, 7, 7).grow(0, 1, -1)}</pre></td><td><pre><samp>{@code box[5, 4, 6 -> 7, 8, 6]}</samp></pre></td></tr>
     * <tr><td><pre>{@code new Region(1, 1, 1, 3, 3, 3).grow(-4, -2, -3)}</pre></td><td><pre><samp>{@code box[-1, 1, 0 -> 5, 3, 4]}</samp></pre></td></tr>
     * </table>
     *
     * <h3>See Also:</h3>
     * <ul>
     * <li>{@link #grow(int)} - version of this that expands in all directions from one parameter.
     * <li>{@link #shrink(int)} - contracts in all directions
     * </ul>
     */
    public Region expand(int x, int y, int z) {
        return new Region(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z);
    }

    public Region expand(Vec3i vec) {
        return expand(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Expand the current region by the given value in the max values. Equivalent to {@link
     * #expand(int)}  with the given value for all 3 params. Negative values will shrink the region.
     * <p>
     * Side lengths will be increased by 2 times the value of the parameter, since both min and max are changed.
     * <p>
     * If contracting and the amount to contract by is larger than the length of a side, then the side will wrap (still
     * creating a valid region - see samples on {@link #grow(int, int, int)}).
     */
    public Region expand(int size) {
        return expand(size, size, size);
    }

    /**
     * @see #expand(int, int, int) - substracting instead of adding.
     */
    public Region collapse(int x, int y, int z) {
        return expand(-x, -y, -z);
    }

    /**
     * @see #collapse(int, int, int) - read x, y, and z from the {@link Vec3i}.
     */
    public Region collapse(Vec3i vec) {
        return collapse(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * @see #collapse(int, int, int) - collapse on all three axes.
     */
    public Region collapse(int size) {
        return expand(-size);
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

    /**
     * Create a new region that encloses both regions
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

    /**
     * Accurate representation of whether the position is a part the structure or not.
     *
     * @see #contains(int, int, int)
     */
    @Override
    public boolean mayContain(int x, int y, int z) {
        return contains(x, y, z);
    }

    /**
     * @return whether or not this {@link BlockPos} lies within this Region
     */
    public boolean contains(int x, int y, int z) {
        return containsX(x) && containsY(y) && containsZ(z);
    }

    public boolean contains(Vec3i vec) {
        return mayContain(vec.getX(), vec.getY(), vec.getZ());
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
     * @return a new Region with the exact same properties
     * @deprecated Since Region is immutable, this is not needed
     */
    @Deprecated
    @Override
    public Region copy() {
        return new Region(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * @return this
     */
    @Override
    public Region getBoundingBox() {
        return this;
    }

    /**
     * The first result will have the minimum x, y, and z value. In the process it will advance in positive z-y-x order as used in BG-Code on various other places.
     * Positions provided by this Iterator may be considered ordered.
     *
     * @return A {@link PeekingIterator} over all positions in this Region
     * @implSpec starts at (minX, minY, minZ), ends at (maxX, maxY, maxZ)
     * @implNote the Iterator does not support removal Operations
     * @see com.direwolf20.buildinggadgets.api.util.CommonUtils#POSITION_COMPARATOR
     */
    @Override
    public PeekingIterator<BlockPos> iterator() {
        return new SpliteratorBackedPeekingIterator<>(spliterator());
    }

    /**
     * Creates a {@link Spliterator} over the positions described by this {@code Region}.
     *
     * @return a {@link Spliterator} over the positions described by this {@code Region}.
     * @implSpec The returned {@link Spliterator} will be Immutable, Sorted and Sized
     */
    @Override
    public Spliterator<BlockPos> spliterator() {
        return new RegionSpliterator(this);
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

    public CompoundNBT serialize() {
        return serializeTo(new CompoundNBT());
    }

    public CompoundNBT serializeTo(CompoundNBT tag) {
        tag.putInt(KEY_MIN_X, minX);
        tag.putInt(KEY_MIN_Y, minY);
        tag.putInt(KEY_MIN_Z, minZ);
        tag.putInt(KEY_MAX_X, maxX);
        tag.putInt(KEY_MAX_Y, maxY);
        tag.putInt(KEY_MAX_Z, maxZ);
        return tag;
    }

    private static class RegionSpliterator implements Spliterator<BlockPos> {

        private int minX;
        private int minY;
        private int minZ;

        private int maxX;
        private int maxY;
        private int maxZ;

        private int nextPosX;
        private int nextPosY;
        private int nextPosZ;

        /**
         * Note that as soon as this spliterator advanced once, it can no longer be guaranteed that the given blocks have not yet been visited
         */
        private boolean allowYZSplit;

        private RegionSpliterator(Region region) {
            this(region.getMinX(), region.getMinY(), region.getMinZ(), region.getMaxX(), region.getMaxY(), region.getMaxZ());
        }

        private RegionSpliterator(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this(minX, minY, minZ, maxX, maxY, maxZ, minX, minY, minZ, true);
        }

        private RegionSpliterator(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int posX, int posY, int posZ, boolean allowYZSplit) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;

            this.nextPosX = posX;
            this.nextPosY = posY;
            this.nextPosZ = posZ;
            this.allowYZSplit = allowYZSplit;
        }

        @Override
        public boolean tryAdvance(Consumer<? super BlockPos> action) {
            if (isXOverflowed())
                return false;

            this.allowYZSplit = false;
            BlockPos pos = new BlockPos(nextPosX, nextPosY, nextPosZ);

            nextPosZ++;
            if (isZOverflowed()) {
                nextPosZ = minZ;
                nextPosY++;
            } else {
                action.accept(pos);
                return true;
            }

            if (isYOverflowed()) {
                nextPosY = minY;
                nextPosX++;
            } else {
                action.accept(pos);
                return true;
            }

            // Returns (maxX, maxY, maxZ)
            action.accept(pos);
            return true;
        }

        /**
         * @return A part of this {@link Spliterator}, if splitting is possible
         */
        @Override
        public Spliterator<BlockPos> trySplit() {
            int oldMinX = minX;
            int oldMinY = minY;
            int oldMinZ = minZ;
            int oldPosX = nextPosX;
            int oldPosY = nextPosY;
            int oldPosZ = nextPosZ;

            // Construct new min coordinates, so that at least one can be split of: max - min >= 1
            if (maxX > minX) {
                /* As Region's coordinates are inclusive, the amount of blocks along one axis is max - min + 1
                 * half the length + base x
                 */
                minX = (maxX - minX + 1) / 2 + minX + 1;
                resetPos();
                return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, minX - 1, maxY, maxZ, oldPosX, oldPosY, oldPosZ, allowYZSplit);
            } else if (maxY > minY && allowYZSplit) {
                minY = (maxY - minY + 1) / 2 + minY + 1;
                resetPos();
                return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, maxX, minY - 1, maxZ, oldPosX, oldPosY, oldPosZ, allowYZSplit);
            } else if (maxZ > minZ && allowYZSplit) {
                minZ = (maxZ - minZ + 1) / 2 + minZ + 1;
                resetPos();
                return new RegionSpliterator(oldMinX, oldMinY, oldMinZ, maxX, maxY, minZ - 1, oldPosX, oldPosY, oldPosZ, allowYZSplit);
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return (Math.abs((long) maxX - nextPosX) + 1) * (Math.abs((long) maxY - nextPosY) + 1) * (Math.abs((long) maxZ - nextPosZ + 1));
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SORTED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        /**
         * @return {@link com.direwolf20.buildinggadgets.api.util.CommonUtils#POSITION_COMPARATOR}
         */
        @Override
        public Comparator<? super BlockPos> getComparator() {
            return CommonUtils.POSITION_COMPARATOR;
        }

        private boolean isXOverflowed() {
            return nextPosX > maxX;
        }

        private boolean isYOverflowed() {
            return nextPosY > maxY;
        }

        private boolean isZOverflowed() {
            return nextPosZ > maxZ;
        }

        private void resetPos() {
            this.nextPosX = minX;
            this.nextPosY = minY;
            this.nextPosZ = minZ;
        }

    }
}