package com.direwolf20.buildinggadgets.building.placementTests;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.placement.Surface;
import com.direwolf20.buildinggadgets.util.CasedBlockView;
import com.google.common.collect.Sets;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("requires custom runner with minecraft started")
public class SurfaceTest {

    private static final Random random = new Random();

    @Test
    void iteratorShouldIgnoreBaseBlock() {
        Region region = new Region(-2, 0, -2, 2, 0, 2);
        CasedBlockView world = new CasedBlockView(region, CasedBlockView.base, CasedBlockView.target);
        Surface surface = Surface.create(world, BlockPos.ORIGIN, EnumFacing.UP, 5, false);

        Set<BlockPos> expected = Sets.newHashSet(region);
        for (BlockPos pos : surface) {
            //Search in the area above region
            assertTrue(expected.contains(pos.down()));
        }
    }

}
