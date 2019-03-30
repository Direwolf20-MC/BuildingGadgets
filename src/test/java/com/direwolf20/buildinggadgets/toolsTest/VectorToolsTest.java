package com.direwolf20.buildinggadgets.toolsTest;

import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Random;

import static net.minecraft.util.EnumFacing.Axis;
import static org.junit.jupiter.api.Assertions.*;

public class VectorToolsTest {

    private final Random random = new Random();

    @RepeatedTest(8)
    void getAxisValueShouldReturnSameValueAsBlockPosGetterMethod() {
        int x = random.nextInt();
        int y = random.nextInt();
        int z = random.nextInt();
        BlockPos pos = new BlockPos(x, y, z);

        assertEquals(pos.getX(), VectorTools.getAxisValue(pos, Axis.X));
        assertEquals(pos.getY(), VectorTools.getAxisValue(pos, Axis.Y));
        assertEquals(pos.getZ(), VectorTools.getAxisValue(pos, Axis.Z));
    }

    @RepeatedTest(2)
    void perpendicularSurfaceOffsetShouldChangeXAndZWithAxisY() {
        int i = random.nextInt();
        int j = random.nextInt();
        BlockPos offset = VectorTools.perpendicularSurfaceOffset(BlockPos.ORIGIN, Axis.Y, i, j);
        assertEquals(BlockPos.ORIGIN.add(i, 0, j), offset);
    }

    @RepeatedTest(2)
    void perpendicularSurfaceOffsetShouldChangeYAndZWithAxisX() {
        int i = random.nextInt();
        int j = random.nextInt();
        BlockPos offset = VectorTools.perpendicularSurfaceOffset(BlockPos.ORIGIN, Axis.X, i, j);
        assertEquals(BlockPos.ORIGIN.add(0, i, j), offset);
    }

    @RepeatedTest(2)
    void perpendicularSurfaceOffsetShouldChangeXAndYWithAxisZ() {
        int i = random.nextInt();
        int j = random.nextInt();
        BlockPos offset = VectorTools.perpendicularSurfaceOffset(BlockPos.ORIGIN, Axis.Z, i, j);
        assertEquals(BlockPos.ORIGIN.add(i, j, 0), offset);
    }

}
