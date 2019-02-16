package com.direwolf20.buildinggadgets.common.tools;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.structure.StructureBoundingBox;

/**
 * <p>Javadoc are copied from {@link AxisAlignedBB} with some modifications.</p>
 */
public final class Region extends StructureBoundingBox {

    public Region(int[] vertexes) {
        super(vertexes);
    }

    public Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public Region(Vec3i vertex) {
        this(vertex, vertex);
    }

    public Region(Vec3i mincVertex, Vec3i maxVertex) {
        super(mincVertex, maxVertex);
    }

    public Region offsetBy(int x, int y, int z) {
        super.offset(x, y, z);
        return this;
    }

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
     * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 1, 1, 1).grow(2, 2, 2)</code></pre></td><td><pre><samp>box[-2.0, -2.0, -2.0 -> 3.0, 3.0, 3.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(0, 0, 0, 6, 6, 6).grow(-2, -2, -2)</code></pre></td><td><pre><samp>box[2.0, 2.0, 2.0 -> 4.0, 4.0, 4.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(5, 5, 5, 7, 7, 7).grow(0, 1, -1)</code></pre></td><td><pre><samp>box[5.0, 4.0, 6.0 -> 7.0, 8.0, 6.0]</samp></pre></td></tr>
     * <tr><td><pre><code>new AxisAlignedBB(1, 1, 1, 3, 3, 3).grow(-4, -2, -3)</code></pre></td><td><pre><samp>box[-1.0, 1.0, 0.0 -> 5.0, 3.0, 4.0]</samp></pre></td></tr>
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
     * @return a new region
     */
    public Region intersect(Region other) {
        this.minX = Math.max(this.minX, other.minX);
        this.minY = Math.max(this.minY, other.minY);
        this.minZ = Math.max(this.minZ, other.minZ);
        this.maxX = Math.min(this.maxX, other.maxX);
        this.maxY = Math.min(this.maxY, other.maxY);
        this.maxZ = Math.min(this.maxZ, other.maxZ);
        return this;
    }

    /**
     * Create a new region that encloses both regions that has the minimum volume.
     * @return a new region
     */
    public Region union(Region other) {
        this.minX = Math.min(this.minX, other.minX);
        this.minY = Math.min(this.minY, other.minY);
        this.minZ = Math.min(this.minZ, other.minZ);
        this.maxX = Math.max(this.maxX, other.maxX);
        this.maxY = Math.max(this.maxY, other.maxY);
        this.maxZ = Math.max(this.maxZ, other.maxZ);
        return this;
    }

    /**
     * @return a new Region with the exact same properties
     */
    public Region copy() {
        return new Region(minX, minY, minZ, maxX, maxY, maxZ);
    }

}
