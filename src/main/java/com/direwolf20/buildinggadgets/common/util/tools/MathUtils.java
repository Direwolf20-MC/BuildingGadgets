package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;

@Tainted(reason = "Shouldn't exist.")
public final class MathUtils {
    public static final int B1_BYTE_MASK = 0xFF;
    public static final int B2_BYTE_MASK = 0xFF_FF;
    public static final int B3_BYTE_MASK = 0xFF_FF_FF;
    public static final long B5_BYTE_MASK = ((long) 0xFF_FF_FF_FF) << 8 | 0xFF;
    private MathUtils() {}

    public static short additiveInverse(short num) {
        return (short) - num;
    }

    /**
     * Converts the BlockPos to a long. Under the assumption, that it is non-negative and does not exceed [0, 255] for the y Coordinate
     * and [0, 65536] for x and z‬ Coordinates.
     * @param pos   BlockPos
     *
     * @return BlockPos to a long
     */
    public static long posToLong(BlockPos pos) {
        long res = (long) (pos.getX() & B2_BYTE_MASK) << 24;
        res |= (pos.getY() & B1_BYTE_MASK) << 16; // y-Positions are in [0,255] inclusive
        res |= (pos.getZ() & B2_BYTE_MASK);
        return res;
    }

    public static BlockPos posFromLong(long serialized) {
        int x = (int) ((serialized >> 24) & B2_BYTE_MASK);
        int y = (int) ((serialized >> 16) & B1_BYTE_MASK);
        int z = (int) (serialized & B2_BYTE_MASK);
        return new BlockPos(x, y, z);
    }

    public static long includeStateId(long serialized, int id) {
        return serialized | ((long) (id & B3_BYTE_MASK) << 40);
    }

    public static int readStateId(long serialized) {
        return (int) ((serialized >> 40) & B3_BYTE_MASK);
    }

    public static long readSerializedPos(long serialized) {
        return serialized & B5_BYTE_MASK;
    }

    public static int floorMultiple(int i, int factor) {
        return i - (i % factor);
    }

    public static int ceilMultiple(int i, int factor) {
        return i + (i % factor);
    }

    public static boolean isEven(int i) {
        return (i & 1) == 0;
    }

    public static boolean isOdd(int i) {
        return i % 2 == 1;
    }

    private static int addForNonEven(int i, int c) {
        return isEven(i) ? i : i + c;
    }

    private static int addForNonOdd(int i, int c) {
        return isOdd(i) ? i : i + c;
    }

    public static int floorToEven(int i) {
        return addForNonEven(i, -1);
    }

    public static int floorToOdd(int i) {
        return addForNonOdd(i, -1);
    }

    public static int ceilToEven(int i) {
        return addForNonEven(i, 1);
    }

    public static int ceilToOdd(int i) {
        return addForNonOdd(i, 1);
    }

    private static int sineForRotation(Rotation rot) {
        switch (rot) {
            case NONE:
            case CLOCKWISE_180:
                return 0;
            case CLOCKWISE_90:
                return 1;
            case COUNTERCLOCKWISE_90:
                return - 1;
            default:
                throw new AssertionError();
        }
    }

    private static int cosineForRotation(Rotation rot) {
        return sineForRotation(rot.getRotated(Rotation.CLOCKWISE_90));
    }

    public static int[][] rotationMatrixFor(Axis axis, Rotation rotation) {
        int[][] matrix = new int[3][3]; //remember it's Java => everything initiated to 0
        switch (axis) {
            case X: {
                matrix[0][0] = 1;
                matrix[1][1] = cosineForRotation(rotation);
                matrix[1][2] = sineForRotation(rotation);
                matrix[2][1] = - sineForRotation(rotation);
                matrix[2][2] = cosineForRotation(rotation);
                break;
            }
            case Y: {
                matrix[1][1] = 1;
                matrix[0][0] = cosineForRotation(rotation);
                matrix[2][0] = sineForRotation(rotation);
                matrix[0][2] = - sineForRotation(rotation);
                matrix[2][2] = cosineForRotation(rotation);
                break;
            }
            case Z: {
                matrix[2][2] = 1;
                matrix[0][0] = cosineForRotation(rotation);
                matrix[0][1] = sineForRotation(rotation);
                matrix[1][0] = - sineForRotation(rotation);
                matrix[1][1] = cosineForRotation(rotation);
                break;
            }
        }
        return matrix;
    }

    public static BlockPos matrixMul(int[][] matrix, BlockPos pos) {
        int x = pos.getX() * matrix[0][0] + pos.getY() * matrix[0][1] + pos.getZ() * matrix[0][2];
        int y = pos.getX() * matrix[1][0] + pos.getY() * matrix[1][1] + pos.getZ() * matrix[1][2];
        int z = pos.getX() * matrix[2][0] + pos.getY() * matrix[2][1] + pos.getZ() * matrix[2][2];
        return new BlockPos(x, y, z);
    }
}
