package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.api.building.placement.Stair;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StairTest {

    private final Random random = new Random();

    @RepeatedTest(4)
    void stairShouldContainSameAmountOfBlocksPlus1AsSizeParameter() {
        int size = random.nextInt(16);
        Stair stair = Stair.create(BlockPos.ORIGIN, EnumFacing.NORTH, EnumFacing.UP, size);

        assertEquals(size + 1, stair.collect().size());
    }

    @RepeatedTest(4)
    void positionShouldOffsetBy1InBothHorizontalAndVerticalDirectionCaseNorthUp() {
        int size = random.nextInt(16);
        Stair stair = Stair.create(BlockPos.ORIGIN, EnumFacing.NORTH, EnumFacing.UP, size);
        Iterator<BlockPos> it = stair.iterator();
        BlockPos last = it.next();
        if (!it.hasNext()) {
            return;
        }

        BlockPos current = it.next();
        while (it.hasNext()) {
            assertEquals(1, Math.abs(current.getY() - last.getY()));
            assertEquals(1, Math.abs(current.getZ() - last.getZ()));

            last = current;
            current = it.next();
        }
    }

}
