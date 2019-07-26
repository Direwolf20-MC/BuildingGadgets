package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.placement.PlacementSequences.Grid;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class GridTest {

    @Test
    void iteratorShouldUse9BlocksInFirstPeriodCasePeriodSizeEquals6() {
        for (int i = 1; i <= 6; i++) {
            IPositionPlacementSequence grid = Grid.create(BlockPos.ZERO, i, 6);
            Iterator<BlockPos> it = grid.iterator();
            for (int j = 0; j < 9; j++) {
                it.next();
            }
            assertFalse(it.hasNext());
        }
    }

}
