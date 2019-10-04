package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.EnumSet;

final class RotateOperator extends AbsSingleRunTransactionOperator {
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
        return sineForRotation(rot.add(Rotation.CLOCKWISE_90));
    }

    private static int[][] rotationMatrixFor(Axis axis, Rotation rotation) {
        int[][] matrix = new int[3][3]; //remember it's Java => everything initiated to 0
        switch (axis) {
            case X: {
                matrix[0][0] = 1;
                matrix[1][1] = cosineForRotation(rotation);
                matrix[1][2] = - sineForRotation(rotation);
                matrix[2][1] = sineForRotation(rotation);
                matrix[2][2] = cosineForRotation(rotation);
                break;
            }
            case Y: {
                matrix[1][1] = 1;
                matrix[0][0] = cosineForRotation(rotation);
                matrix[2][0] = - sineForRotation(rotation);
                matrix[0][2] = sineForRotation(rotation);
                matrix[2][2] = cosineForRotation(rotation);
                break;
            }
            case Z: {
                matrix[2][2] = 1;
                matrix[0][0] = cosineForRotation(rotation);
                matrix[0][1] = - sineForRotation(rotation);
                matrix[1][0] = sineForRotation(rotation);
                matrix[1][1] = cosineForRotation(rotation);
                break;
            }
        }
        return matrix;
    }

    //[row][column]
    private final int[][] matrix;
    private final Rotation rot;

    RotateOperator(Rotation rotation) { //sadly blockstates only allow rotating around the y axis
        this(Axis.Y, rotation);
    }

    RotateOperator(Axis axis, Rotation rotation) {
        super(EnumSet.of(TransactionOperation.TRANSFORM_TARGET));
        this.matrix = rotationMatrixFor(axis, rotation);
        this.rot = axis == Axis.Y ? rotation : Rotation.NONE;
    }

    @Nullable
    @Override
    public PlacementTarget transformTarget(ITransactionExecutionContext context, PlacementTarget target) {
        return super.transformTarget(context, new PlacementTarget(matrixMul(target.getPos()), target.getData().rotate(rot)));
    }

    private BlockPos matrixMul(BlockPos pos) {
        int x = pos.getX() * matrix[0][0] + pos.getY() * matrix[0][1] + pos.getZ() * matrix[0][2];
        int y = pos.getX() * matrix[1][0] + pos.getY() * matrix[1][1] + pos.getZ() * matrix[1][2];
        int z = pos.getX() * matrix[2][0] + pos.getY() * matrix[2][1] + pos.getZ() * matrix[2][2];
        return new BlockPos(x, y, z);
    }
}
