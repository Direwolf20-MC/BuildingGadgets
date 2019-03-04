package com.direwolf20.buildinggadgets.api.abstraction;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.Vec3i;

public final class ImmutablePos3s extends Pos3s {
    public static final ImmutablePos3s ORIGIN = new ImmutablePos3s(0, 0, 0);
    public ImmutablePos3s(Pos3s pos) {
        super(pos);
    }

    public ImmutablePos3s(Vec3i vec) {
        super(vec);
    }

    public ImmutablePos3s(int x, int y, int z) {
        super(x, y, z);
    }

    public ImmutablePos3s(short x, short y, short z) {
        super(x, y, z);
    }

    @Override
    public ImmutablePos3s setTo(Vec3i vec) {
        return new ImmutablePos3s(vec);
    }

    @Override
    public ImmutablePos3s setTo(Pos3s vec) {
        return new ImmutablePos3s(vec);
    }

    @Override
    public ImmutablePos3s setTo(int x, int y, int z) {
        return new ImmutablePos3s(x, y, z);
    }

    @Override
    public ImmutablePos3s setTo(short x, short y, short z) {
        return new ImmutablePos3s(x, y, z);
    }

    /**
     * Add the given Vector to this BlockPos
     *
     * @param vec the {@link Vec3i} to add
     * @see #add(int, int, int)
     */
    @Override
    public ImmutablePos3s add(Vec3i vec) {
        return setTo(getX() + vec.getX(), getY() + vec.getY(), getZ() + vec.getZ());
    }

    /**
     * Add the given Vector to this BlockPos
     *
     * @param vec the Pos3s to add
     * @see #add(int, int, int)
     */
    @Override
    public ImmutablePos3s add(Pos3s vec) {
        return setTo(getX() + vec.getX(), getY() + vec.getY(), getZ() + vec.getZ());
    }

    /**
     * Add the given coordinates to the coordinates of this Pos3s
     *
     * @param x the x add
     * @param y the y add
     * @param z the z add
     */
    @Override
    public ImmutablePos3s add(int x, int y, int z) {
        return setTo(getX() + x, getY() + y, getZ() + z);
    }

    /**
     * Add the given coordinates to the coordinates of this Pos3s.
     * <b>Note that no Overflow checking is performed!</b>
     *
     * @param x the x add
     * @param y the y add
     * @param z the z add
     */
    @Override
    public ImmutablePos3s add(short x, short y, short z) {
        return setTo(getX() + x, getY() + y, getZ() + z);
    }

    /**
     * Subtract the given Pos3s from this BlockPos
     *
     * @param vec The vector to subtract
     */
    @Override
    public ImmutablePos3s subtract(Pos3s vec) {
        return setTo(getX() - vec.getX(), getY() - vec.getY(), getZ() - vec.getZ());
    }

    /**
     * Subtract the given {@link Vec3i} from this BlockPos
     *
     * @param vec The vector to subtract
     */
    @Override
    public ImmutablePos3s subtract(Vec3i vec) {
        return setTo(getX() - vec.getX(), getY() - vec.getY(), getZ() - vec.getZ());
    }

    /**
     * Subtract the given coordinates from this Pos3s
     *
     * @param x the amount to subtract from x
     * @param y the amount to subtract from y
     * @param z the amount to subtract from z
     */
    @Override
    public ImmutablePos3s subtract(int x, int y, int z) {
        return setTo(getX() - x, getY() - y, getZ() - z);
    }

    /**
     * Subtract the given coordinates from this Pos3s
     *
     * @param x the amount to subtract from x
     * @param y the amount to subtract from y
     * @param z the amount to subtract from z
     */
    @Override
    public ImmutablePos3s subtract(short x, short y, short z) {
        return setTo(getX() - x, getY() - y, getZ() - z);
    }

    /**
     * Returns a version of this Pos3s that is guaranteed to be immutable.
     *
     * <p>When storing a BlockPos given to you for an extended period of time, make sure you
     * use this in case the value is changed internally.</p>
     */
    @Override
    public ImmutablePos3s toImmutable() {
        return this;
    }

    /**
     * Offset this Pos3s 1 block up
     *
     * @see #up(short)
     */
    @Override
    public ImmutablePos3s up() {
        return up((short) 1);
    }

    /**
     * Offset this Pos3s n blocks up
     *
     * @param n The amount to offset
     * @see #offset(EnumFacing, short)
     */
    @Override
    public ImmutablePos3s up(short n) {
        return offset(EnumFacing.UP, n);
    }

    /**
     * Offset this Pos3s 1 block down
     *
     * @see #down(short)
     */
    @Override
    public ImmutablePos3s down() {
        return down((short) 1);
    }

    /**
     * Offset this BlockPos n blocks down
     *
     * @param n the amount to offset
     * @see #offset(EnumFacing, short)
     */
    @Override
    public ImmutablePos3s down(short n) {
        return offset(EnumFacing.DOWN, n);
    }

    /**
     * Offset this Pos3s 1 block in northern direction
     *
     * @see #north(short)
     */
    @Override
    public ImmutablePos3s north() {
        return north((short) 1);
    }

    /**
     * Offset this Pos3s n blocks in northern direction
     *
     * @param n the amount to offset
     * @see #offset(EnumFacing, short)
     */
    @Override
    public ImmutablePos3s north(short n) {
        return offset(EnumFacing.NORTH, n);
    }

    /**
     * Offset this Pos3s 1 block in southern direction
     *
     * @see #south(short)
     */
    @Override
    public ImmutablePos3s south() {
        return south((short) 1);
    }

    /**
     * Offset this Pos3s n blocks in southern direction
     *
     * @param n the amount to offset
     * @see #offset(EnumFacing, short)
     */
    @Override
    public ImmutablePos3s south(short n) {
        return offset(EnumFacing.SOUTH, n);
    }

    /**
     * Offset this Pos3s 1 block in western direction
     *
     * @see #west(short)
     */
    @Override
    public ImmutablePos3s west() {
        return west((short) 1);
    }

    /**
     * Offset this Pos3s n blocks in western direction
     *
     * @param n the amount to offset
     * @see #offset(EnumFacing, short)
     */
    @Override
    public ImmutablePos3s west(short n) {
        return offset(EnumFacing.WEST, (short) 1);
    }

    /**
     * Offset this Pos3s 1 block in eastern direction
     *
     * @see #east(short)
     */
    @Override
    public ImmutablePos3s east() {
        return east((short) 1);
    }

    /**
     * Offset this Pos3s n blocks in eastern direction
     *
     * @param n The amount to offset
     */
    @Override
    public ImmutablePos3s east(short n) {
        return offset(EnumFacing.EAST, n);
    }

    /**
     * Calculate the cross product of this and the given Vector
     *
     * @param vec the {@link Vec3i} to calc crossProduct with
     * @see #crossProduct(int, int, int)
     */
    @Override
    public ImmutablePos3s crossProduct(Vec3i vec) {
        return crossProduct(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Calculate the cross product of this and the given Pos3s
     *
     * @param vec the Pos3s to calc crossProduct with
     * @see #crossProduct(int, int, int)
     */
    @Override
    public ImmutablePos3s crossProduct(Pos3s vec) {
        return crossProduct(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * @param x x-Coordinate
     * @param y y-Coordinate
     * @param z z-Coordinate
     */
    @Override
    public ImmutablePos3s crossProduct(int x, int y, int z) {
        return setTo(getX() * z - getZ() * y, getZ() * x - getX() * z, getX() * y - getY() * x);
    }

    /**
     * @param x x-Coordinate
     * @param y y-Coordinate
     * @param z z-Coordinate
     */
    @Override
    public ImmutablePos3s crossProduct(short x, short y, short z) {
        return setTo(getX() * z - getZ() * y, getZ() * x - getX() * z, getX() * y - getY() * x);
    }

    /**
     * Offset this Pos3s 1 block in the given direction
     *
     * @param facing the {@link EnumFacing} by which to offset
     */
    @Override
    public ImmutablePos3s offset(EnumFacing facing) {
        return offset(facing, (short) 1);
    }

    /**
     * Offsets this Pos3s n blocks in the given direction
     *
     * @param facing The {@link EnumFacing} in which to offset
     * @param n      The amount to offset by
     */
    @Override
    public ImmutablePos3s offset(EnumFacing facing, short n) {
        return add(facing.getXOffset() * n, facing.getYOffset() * n, facing.getZOffset() * n);
    }

    /**
     * Rotates this Pos3s by the given {@link Rotation} around the x-Axis
     * @param rotationIn The rotation to perform
     * @see #rotate(Rotation, EnumFacing.Axis)
     */
    @Override
    public ImmutablePos3s rotateX(Rotation rotationIn) {
        return (ImmutablePos3s) super.rotateX(rotationIn);
    }

    /**
     * Rotates this Pos3s by the given {@link Rotation} around the y-Axis
     * @param rotationIn The rotation to perform
     * @see #rotate(Rotation, EnumFacing.Axis)
     */
    @Override
    public ImmutablePos3s rotateY(Rotation rotationIn) {
        return (ImmutablePos3s) super.rotateY(rotationIn);
    }

    /**
     * Rotates this Pos3s by the given {@link Rotation} around the z-Axis
     * @param rotationIn The rotation to perform
     * @see #rotate(Rotation, EnumFacing.Axis)
     */
    @Override
    public ImmutablePos3s rotateZ(Rotation rotationIn) {
        return (ImmutablePos3s) super.rotateZ(rotationIn);
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
    @Override
    public ImmutablePos3s rotate(Rotation rotation, EnumFacing.Axis axis) {
        return (ImmutablePos3s) super.rotate(rotation, axis);
    }
}
