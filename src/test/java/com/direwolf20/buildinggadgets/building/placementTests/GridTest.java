package com.direwolf20.buildinggadgets.building.placementTests;

import com.direwolf20.buildinggadgets.common.building.placement.Grid;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class GridTest {

    @Test
    void iteratorShouldUse9BlocksInFirstPeriodCasePeriodSizeEquals6() {
        for (int i = 1; i <= 6; i++) {
            Grid grid = Grid.create(BlockPos.ORIGIN, i, 6);
            Iterator<BlockPos> it = grid.iterator();
            for (int j = 0; j < 9; j++) {
                it.next();
            }
            assertFalse(it.hasNext());
        }
    }

}
