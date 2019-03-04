package com.direwolf20.buildinggadgets.api.abstraction;

import com.google.common.base.MoreObjects;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;

import static com.direwolf20.buildinggadgets.api.utils.MathUtils.additiveInverse;
import static com.google.common.primitives.Shorts.checkedCast;

public class Pos3s implements Comparable<Pos3s> {
    private short x;
    private short y;
    private short z;

    public Pos3s(Pos3s pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Pos3s(Vec3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public Pos3s(int x, int y, int z) {
        this(checkedCast(x), checkedCast(y), checkedCast(z));
    }

    public Pos3s(short x, short y, short z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Gets the X coordinate.
     */
    public short getX() {
        return x;
    }

    /**
     * Gets the Y coordinate.
     */
    public short getY() {
        return y;
    }

    /**
     * Gets the Z coordinate.
     */
    public short getZ() {
        return z;
    }

    protected void setX(short x) {
        this.x = x;
    }

    protected void setY(short y) {
        this.y = y;
    }

    protected void setZ(short z) {
        this.z = z;
    }

    public Pos3s setTo(Vec3i vec) {
        return setTo(vec.getX(), vec.getY(), vec.getZ());
    }

    public Pos3s setTo(Pos3s vec) {
        return setTo(vec.getX(), vec.getY(), vec.getZ());
    }

    public Pos3s setTo(int x, int y, int z) {
        return setTo(checkedCast(x), checkedCast(y), checkedCast(z));
    }

    public Pos3s setTo(short x, short y, short z) {
        setX(x);
        setY(y);
        setZ(z);
        return this;
    }

    /**
     * Add the given Vector to this BlockPos
     *
     * @param vec the {@link Vec3i} to add
     * @see #add(int, int, int)
     */
    public Pos3s add(Vec3i vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Add the given Vector to this BlockPos
     *
     * @param vec the Pos3s to add
     * @see #add(int, int, int)
     */
    public Pos3s add(Pos3s vec) {
        return add(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Add the given coordinates to the coordinates of this Pos3s
     *
     * @param x the x add
     * @param y the y add
     * @param z the z add
     */
    public Pos3s add(int x, int y, int z) {
        return add(checkedCast(x), checkedCast(y), checkedCast(z));
    }

    /**
     * Add the given coordinates to the coordinates of this Pos3s.
     * <b>Note that no Overflow checking is performed!</b>
     *
     * @param x the x add
     * @param y the y add
     * @param z the z add
     */
    public Pos3s add(short x, short y, short z) {
        return setTo(getX() + x, getY() + y, getZ() + z);
    }

    /**
     * Subtract the given Pos3s from this BlockPos
     *
     * @param vec The vector to subtract
     */
    public Pos3s subtract(Pos3s vec) {
        return subtract(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Subtract the given {@link Vec3i} from this BlockPos
     *
     * @param vec The vector to subtract
     */
    public Pos3s subtract(Vec3i vec) {
        return subtract(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Subtract the given coordinates from this Pos3s
     *
     * @param x the amount to subtract from x
     * @param y the amount to subtract from y
     * @param z the amount to subtract from z
     */
    public Pos3s subtract(int x, int y, int z) {
        return subtract(checkedCast(x), checkedCast(y), checkedCast(z));
    }

    /**
     * Subtract the given coordinates from this Pos3s
     *
     * @param x the amount to subtract from x
     * @param y the amount to subtract from y
     * @param z the amount to subtract from z
     */
    public Pos3s subtract(short x, short y, short z) {
        return add(additiveInverse(x), additiveInverse(y), additiveInverse(z));
    }

    /**
     * Returns a version of this Pos3s that is guaranteed to be immutable.
     *
     * <p>When storing a BlockPos given to you for an extended period of time, make sure you
     * use this in case the value is changed internally.</p>
     */
    public ImmutablePos3s toImmutable() {
        return new ImmutablePos3s(getX(), getY(), getZ());
    }

    public BlockPos toBlockPos() {
        return new BlockPos(getX(), getY(), getZ());
    }

    public MutableBlockPos toMutableBlockPos() {
        return new MutableBlockPos(getX(), getY(), getZ());
    }

    /**
     * Offset this Pos3s 1 block up
     *
     * @see #up(short)
     */
    public Pos3s up() {
        return up((short) 1);
    }

    /**
     * Offset this Pos3s n blocks up
     *
     * @param n The amount to offset
     * @see #offset(EnumFacing, short)
     */
    public Pos3s up(short n) {
        return offset(EnumFacing.UP, n);
    }

    /**
     * Offset this Pos3s 1 block down
     *
     * @see #down(short)
     */
    public Pos3s down() {
        return down((short) 1);
    }

    /**
     * Offset this BlockPos n blocks down
     *
     * @param n the amount to offset
     * @see #offset(EnumFacing, short)
     */
    public Pos3s down(short n) {
        return offset(EnumFacing.DOWN, n);
    }

    /**
     * Offset this Pos3s 1 block in northern direction
     *
     * @see #north(short)
     */
    public Pos3s north() {
        return north((short) 1);
    }

    /**
     * Offset this Pos3s n blocks in northern direction
     *
     * @param n the amount to offset
     * @see #offset(EnumFacing, short)
     */
    public Pos3s north(short n) {
        return offset(EnumFacing.NORTH, n);
    }

    /**
     * Offset this Pos3s 1 block in southern direction
     *
     * @see #south(short)
     */
    public Pos3s south() {
        return south((short) 1);
    }

    /**
     * Offset this Pos3s n blocks in southern direction
     *
     * @param n the amount to offset
     * @see #offset(EnumFacing, short)
     */
    public Pos3s south(short n) {
        return offset(EnumFacing.SOUTH, n);
    }

    /**
     * Offset this Pos3s 1 block in western direction
     *
     * @see #west(short)
     */
    public Pos3s west() {
        return west((short) 1);
    }

    /**
     * Offset this Pos3s n blocks in western direction
     *
     * @param n the amount to offset
     * @see #offset(EnumFacing, short)
     */
    public Pos3s west(short n) {
        return offset(EnumFacing.WEST, n);
    }

    /**
     * Offset this Pos3s 1 block in eastern direction
     *
     * @see #east(short)
     */
    public Pos3s east() {
        return east((short) 1);
    }

    /**
     * Offset this Pos3s n blocks in eastern direction
     *
     * @param n The amount to offset
     * @see #offset(EnumFacing, short)
     */
    public Pos3s east(short n) {
        return offset(EnumFacing.EAST, n);
    }

    /**
     * Calculate the cross product of this and the given Vector
     *
     * @param vec the {@link Vec3i} to calc crossProduct with
     * @see #crossProduct(int, int, int)
     */
    public Pos3s crossProduct(Vec3i vec) {
        return crossProduct(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Calculate the cross product of this and the given Pos3s
     *
     * @param pos the Pos3s to calc crossProduct with
     * @see #crossProduct(int, int, int)
     */
    public Pos3s crossProduct(Pos3s pos) {
        return crossProduct(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @param x x-Coordinate
     * @param y y-Coordinate
     * @param z z-Coordinate
     */
    public Pos3s crossProduct(int x, int y, int z) {
        return crossProduct(checkedCast(x), checkedCast(y), checkedCast(z));
    }

    /**
     * @param x x-Coordinate
     * @param y y-Coordinate
     * @param z z-Coordinate
     */
    public Pos3s crossProduct(short x, short y, short z) {
        return setTo(getY() * z - getZ() * y, getZ() * x - getX() * z, getX() * y - getY() * x);
    }

    /**
     * Offset this Pos3s 1 block in the given direction
     *
     * @param facing the {@link EnumFacing} by which to offset
     */
    public Pos3s offset(EnumFacing facing) {
        return offset(facing, (short) 1);
    }

    /**
     * Offsets this Pos3s n blocks in the given direction
     *
     * @param facing The {@link EnumFacing} in which to offset
     * @param n      The amount to offset by
     */
    public Pos3s offset(EnumFacing facing, short n) {
        return setTo(getX() + facing.getXOffset() * n, getY() + facing.getYOffset() * n, getZ() + facing.getZOffset() * n);
    }

    /**
     * Rotates this Pos3s by the given {@link Rotation} around the x-Axis
     *
     * @param rotationIn The rotation to perform
     * @see #rotate(Rotation, EnumFacing.Axis)
     */
    public Pos3s rotateX(Rotation rotationIn) {
        switch (rotationIn) {
            case CLOCKWISE_90:
                return setTo(getX(), getZ(), additiveInverse(getY()));
            case CLOCKWISE_180:
                return setTo(getX(), additiveInverse(getY()), additiveInverse(getZ()));
            case COUNTERCLOCKWISE_90:
                return setTo(getX(), additiveInverse(getZ()), getY());
            default:
                return this;
        }
    }

    /**
     * Rotates this Pos3s by the given {@link Rotation} around the y-Axis
     *
     * @param rotationIn The rotation to perform
     * @see #rotate(Rotation, EnumFacing.Axis)
     */
    public Pos3s rotateY(Rotation rotationIn) {
        switch (rotationIn) {
            case CLOCKWISE_90:
                return setTo(additiveInverse(getZ()), getY(), getX());
            case CLOCKWISE_180:
                return setTo(additiveInverse(getX()), getY(), additiveInverse(getZ()));
            case COUNTERCLOCKWISE_90:
                return setTo(getZ(), getY(), additiveInverse(getX()));
            default:
                return this;
        }
    }

    /**
     * Rotates this Pos3s by the given {@link Rotation} around the z-Axis
     *
     * @param rotationIn The rotation to perform
     * @see #rotate(Rotation, EnumFacing.Axis)
     */
    public Pos3s rotateZ(Rotation rotationIn) {
        switch (rotationIn) {
            case CLOCKWISE_90:
                return setTo(getY(), additiveInverse(getX()), getZ());
            case CLOCKWISE_180:
                return setTo(additiveInverse(getX()), additiveInverse(getY()), getZ());
            case COUNTERCLOCKWISE_90:
                return setTo(additiveInverse(getY()), getX(), getZ());
            default:
                return this;
        }
    }

    /**
     * Rotates this Pos3s by the given {@link Rotation} around the specified {@link EnumFacing.Axis}.
     * The Rotation is treated as if looking towards the negative values of the specified {@code Axis}.
     * Axis are treated as MC axis, so that negative z becomes north and positive x east, when looking at 2D plane, which
     * would be given by the y-Axis as Normal.
     * @param rotation The {@link Rotation} to perform
     * @param axis The {@link EnumFacing.Axis} around which to rotate
     * @return a Pos3s rotated around the specified axis by the specified rotation
     */
    public Pos3s rotate(Rotation rotation, EnumFacing.Axis axis) {
        switch (axis) {
            case X:
                return rotateX(rotation);
            case Y:
                return rotateY(rotation);
            case Z:
                return rotateZ(rotation);
            default:
                throw new IllegalArgumentException("Attempted to rotate by unknown Axis " + axis);
        }
    }

    public double getDistance(Pos3s other) {
        return getDistance(other.getX(), other.getY(), other.getZ());
    }

    public double getDistance(Vec3i other) {
        return getDistance(other.getX(), other.getY(), other.getZ());
    }

    public double getDistance(int x, int y, int z) {
        return Math.sqrt(distanceSq(x, y, z));
    }

    /**
     * Calculate squared distance to the given {@link Vec3i}
     */
    public double distanceSq(Vec3i to) {
        return distanceSq(to.getX(), to.getY(), to.getZ());
    }

    /**
     * Calculate squared distance to the given Pos3s
     */
    public double distanceSq(Pos3s to) {
        return distanceSq(to.getX(), to.getY(), to.getZ());
    }

    /**
     * Calculate squared distance to the given coordinates
     */
    public double distanceSq(int x, int y, int z) {
        return distanceSq((double) x, (double) y, (double) z);
    }

    /**
     * Calculate squared distance to the given coordinates
     *
     * @param toX target x
     * @param toY target y
     * @param toZ target z
     */
    public double distanceSq(double toX, double toY, double toZ) {
        double x1 = x - toX;
        double x2 = y - toY;
        double x3 = z - toZ;
        return x1 * x1 + x2 * x2 + x3 * x3;
    }

    public double getDistanceToCenter(Pos3s other) {
        return getDistanceToCenter(other.getX(), other.getY(), other.getZ());
    }

    public double getDistanceToCenter(Vec3i other) {
        return getDistanceToCenter(other.getX(), other.getY(), other.getZ());
    }

    public double getDistanceToCenter(int x, int y, int z) {
        return Math.sqrt(distanceSqToCenter(x, y, z));
    }

    public double distanceSqToCenter(int x, int y, int z) {
        return distanceSqToCenter((double) x, (double) y, (double) z);
    }

    /**
     * Compute square of distance from point x, y, z to center of the Block represented by this Pos3s
     */
    public double distanceSqToCenter(double toX, double toY, double toZ) {
        return distanceSq(toX - 0.5, toY - 0.5, toZ - 0.5);
    }

    /**
     * @param pos The pos to compare to
     * @return comparison in x-y-z order
     */
    @Override
    public int compareTo(Pos3s pos) {
        int compare = Short.compare(getX(), pos.getX());
        if (compare != 0) return compare;
        compare = Short.compare(getY(), pos.getY());
        if (compare != 0) return compare;
        return Short.compare(getZ(), pos.getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof Pos3s)) return false;

        Pos3s pos3s = (Pos3s) o;

        if (getX() != pos3s.getX()) return false;
        if (getY() != pos3s.getY()) return false;
        return getZ() == pos3s.getZ();
    }

    @Override
    public int hashCode() {
        int result = getX();
        result = 31 * result + getY();
        result = 31 * result + getZ();
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("x", getX())
                .add("y", getY())
                .add("z", getZ())
                .toString();
    }
}
