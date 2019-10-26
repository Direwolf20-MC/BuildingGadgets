package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.placement.PlacementSequences.Wall;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class WallTest {

    private final Random random = new Random();

    @RepeatedTest(8)
    void clickedSideReturnsCorrectWall() {
        int range = random.nextInt(16);
        int size = 2 * range + 1;
        IPositionPlacementSequence wall = Wall.clickedSide(BlockPos.ZERO, Direction.UP, range);

        assertEquals(size, wall.getBoundingBox().getXSize());
        assertEquals(size, wall.getBoundingBox().getZSize());
        assertEquals(1, wall.getBoundingBox().getYSize());
        for (BlockPos pos : wall) {
            assertTrue(pos.getX() <= range);
            assertTrue(pos.getZ() <= range);
            assertEquals(0, pos.getY());
        }
    }

    @Test
    void clickedSideWithFacingUpShouldReturnWallWithSameYCase5By5RandomSizeRandomYLevel() {
        int y = random.nextInt() - 16;
        IPositionPlacementSequence wall = Wall.clickedSide(BlockPos.ZERO.up(y), Direction.UP, random.nextInt(16));
        for (BlockPos pos : wall) {
            assertEquals(y, pos.getY());
        }
    }

    @Test
    void clickedSideWithFacingDownShouldReturnWallWithSameYCase5By5() {
        int y = random.nextInt() - 16;
        IPositionPlacementSequence wall = Wall.clickedSide(BlockPos.ZERO.up(y), Direction.DOWN, random.nextInt(16));
        for (BlockPos pos : wall) {
            assertEquals(y, pos.getY());
        }
    }

    @Test
    void clickedSideWithRange0ShouldReturnWallWithSize1() {
        IPositionPlacementSequence wall = Wall.clickedSide(BlockPos.ZERO, Direction.UP, 0);
        assertEquals(1, wall.getBoundingBox().size());
    }

    @Test
    void extendingFromWithExtensionUpFlatSideNorthRange5ShouldHaveAllPositionsWithSameZHardcoded() {
        IPositionPlacementSequence wall = Wall.extendingFrom(BlockPos.ZERO, Direction.UP, Direction.NORTH, 5, 0);
        for (BlockPos pos : wall) {
            assertEquals(0, pos.getZ());
        }
    }

    @Test
    void extendingFromShouldRejectRequestWithSameExtensionAndFlatSide() {
        for (Direction side : Direction.values()) {
            assertThrows(IllegalArgumentException.class, () -> Wall.extendingFrom(BlockPos.ZERO, side, side, 5, 0));
        }
    }

}
