package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.placement.PlacementSequences.Stair;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StairTest {

    private final Random random = new Random();

    @RepeatedTest(4)
    void stairShouldContainSameAmountOfBlocksAsSizeParameter() {
        int size = random.nextInt(16);
        IPositionPlacementSequence stair = Stair.create(BlockPos.ZERO, Direction.NORTH, Direction.UP, size);

        assertEquals(size, stair.collect().size());
    }

    @RepeatedTest(4)
    void positionShouldOffsetBy1InBothHorizontalAndVerticalDirectionCaseNorthUp() {
        int size = random.nextInt(16);
        IPositionPlacementSequence stair = Stair.create(BlockPos.ZERO, Direction.NORTH, Direction.UP, size);
        Iterator<BlockPos> it = stair.iterator();
        assertTrue(it.hasNext());
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
