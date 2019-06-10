package com.direwolf20.buildinggadgets.apiTest.toolsTest;

import com.direwolf20.buildinggadgets.api.util.VectorUtils;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Random;

import static net.minecraft.util.Direction.Axis;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VectorToolsTest {

    private final Random random = new Random();

    @RepeatedTest(8)
    void getAxisValueShouldReturnSameValueAsBlockPosGetterMethod() {
        int x = random.nextInt();
        int y = random.nextInt();
        int z = random.nextInt();
        BlockPos pos = new BlockPos(x, y, z);

        assertEquals(pos.getX(), VectorUtils.getAxisValue(pos, Axis.X));
        assertEquals(pos.getY(), VectorUtils.getAxisValue(pos, Axis.Y));
        assertEquals(pos.getZ(), VectorUtils.getAxisValue(pos, Axis.Z));
    }

    @RepeatedTest(2)
    void perpendicularSurfaceOffsetShouldChangeXAndZWithAxisY() {
        int i = random.nextInt();
        int j = random.nextInt();
        BlockPos offset = VectorUtils.perpendicularSurfaceOffset(BlockPos.ZERO, Axis.Y, i, j);
        assertEquals(BlockPos.ZERO.add(i, 0, j), offset);
    }

    @RepeatedTest(2)
    void perpendicularSurfaceOffsetShouldChangeYAndZWithAxisX() {
        int i = random.nextInt();
        int j = random.nextInt();
        BlockPos offset = VectorUtils.perpendicularSurfaceOffset(BlockPos.ZERO, Axis.X, i, j);
        assertEquals(BlockPos.ZERO.add(0, i, j), offset);
    }

    @RepeatedTest(2)
    void perpendicularSurfaceOffsetShouldChangeXAndYWithAxisZ() {
        int i = random.nextInt();
        int j = random.nextInt();
        BlockPos offset = VectorUtils.perpendicularSurfaceOffset(BlockPos.ZERO, Axis.Z, i, j);
        assertEquals(BlockPos.ZERO.add(i, j, 0), offset);
    }

}
