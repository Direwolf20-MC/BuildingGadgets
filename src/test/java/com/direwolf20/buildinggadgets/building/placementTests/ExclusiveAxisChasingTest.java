package com.direwolf20.buildinggadgets.building.placementTests;

import com.direwolf20.buildinggadgets.common.building.placement.ExclusiveAxisChasing;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ExclusiveAxisChasingTest {

    private final Random random = new Random();

    private Axis randomAxis() {
        Axis[] axises = Axis.values();
        return axises[random.nextInt(axises.length)];
    }

    @Test
    void sequenceShouldNotContainSourceAndTargetPositionRandom() {
        BlockPos source = new BlockPos(random.nextInt(3) - 2, random.nextInt(3) - 2, random.nextInt(3) - 2);
        BlockPos target = new BlockPos(random.nextInt(3) - 2, random.nextInt(3) - 2, random.nextInt(3) - 2);

        ExclusiveAxisChasing sequence = ExclusiveAxisChasing.create(source, target, randomAxis(), Integer.MAX_VALUE);
        for (BlockPos pos : sequence) {
            assertNotEquals(source, pos);
            assertNotEquals(target, pos);
        }
    }

    @Test
    void sequenceShouldHaveDifferenceAndMinimum0AsSizeCaseRandom() {
        BlockPos source = new BlockPos(random.nextInt(3) - 2, random.nextInt(3) - 2, random.nextInt(3) - 2);
        BlockPos target = new BlockPos(random.nextInt(3) - 2, random.nextInt(3) - 2, random.nextInt(3) - 2);
        Axis axis = randomAxis();
        int difference = VectorTools.getAxisValue(source, axis) - VectorTools.getAxisValue(target, axis);
        int expected = difference <= 1 ? 0 : Math.abs(difference);

        ExclusiveAxisChasing sequence = ExclusiveAxisChasing.create(source, target, randomAxis(), Integer.MAX_VALUE);
        assertEquals(expected, sequence.collect().size());
    }

    @Test
    void sequenceShouldHaveDifferenceMinus1AndMinimum0SizeCaseSourceAndTargetOffsetBy1HardCoded() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            //X changed by 1
            ExclusiveAxisChasing sequence = ExclusiveAxisChasing.create(new BlockPos(13, 43, - 424), new BlockPos(14, 43, - 424), facing, Integer.MAX_VALUE);
            assertEquals(0, sequence.collect().size());
        }
    }

    @Test
    void sequenceShouldHaveDifferenceMinus1AndMinimum0SizeCaseSourceAndTargetOffsetBy1Random() {
        EnumFacing facing = EnumFacing.random(random);
        ExclusiveAxisChasing sequence = ExclusiveAxisChasing.create(BlockPos.ORIGIN, BlockPos.ORIGIN.offset(facing), facing, Integer.MAX_VALUE);
        assertEquals(0, sequence.collect().size());
    }

    @Test
    void sequenceShouldHaveDifferenceMinus1AndMinimum0SizeCaseSameSourceAndTargetRandom() {
        EnumFacing facing = EnumFacing.random(random);
        int i = random.nextInt(32);
        BlockPos pos = BlockPos.ORIGIN.offset(facing, i);
        ExclusiveAxisChasing sequence = ExclusiveAxisChasing.create(pos, pos, facing, Integer.MAX_VALUE);
        assertEquals(0, sequence.collect().size());
    }

}
