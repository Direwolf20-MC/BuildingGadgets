package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.placement.ExclusiveAxisChasing;
import com.direwolf20.buildinggadgets.test.util.BlockTestUtils;
import com.direwolf20.buildinggadgets.test.util.annotations.LargeTest;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExclusiveAxisChasingTest {

    private static Random random;

    @BeforeAll
    static void init() {
        random = new Random();
    }

    private void verify(ExclusiveAxisChasing axisChasing, BlockPos source, BlockPos end, Direction dir, int max) {
        List<BlockPos> positions = axisChasing.collect();
        assertEquals(positions.size(), max, () -> "Expected exactly " + max + " positions to be present, but found " + positions.size());
        for (int i = 0; i < max; i++) {
            BlockPos pos = source.offset(dir, i);
            assertTrue(positions.contains(pos), () -> "Expected positions between " + source + " and " + end + " in dir" + false + " to contain " + pos);
        }
    }

    @LargeTest
    void testAllFacingsRandomly() {
        Region testRegion = BlockTestUtils.randomRegion();
        BlockPos p1 = BlockTestUtils.randomBlockPosIn(testRegion);
        BlockPos p2 = BlockTestUtils.randomBlockPosIn(testRegion);
        int max = random.nextInt(15);
        for (Direction facing : Direction.values()) {
            ExclusiveAxisChasing seq = ExclusiveAxisChasing.create(p1, p2, facing, max);
            verify(seq, p1, p2, facing, max);
        }
    }

}
