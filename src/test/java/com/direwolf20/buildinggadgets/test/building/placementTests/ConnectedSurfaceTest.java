package com.direwolf20.buildinggadgets.test.building.placementTests;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.placement.PlacementSequences.ConnectedSurface;
import com.direwolf20.buildinggadgets.common.util.tools.VectorUtils;
import com.direwolf20.buildinggadgets.test.util.CasedBlockView;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

import static com.direwolf20.buildinggadgets.test.util.CasedBlockView.regionAtOriginWithRandomTargets;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("needs improvements of the UniqueBlockState to handle air properly")
public class ConnectedSurfaceTest {

    private final Random random = new Random();

    @Test
    void connectedSurfaceShouldUseFuzzyMayContainsToRegion5By5() {
        CasedBlockView world = new CasedBlockView(new Region(-2, 0, -2, 2, 0, 2), CasedBlockView.base, CasedBlockView.target);
        IPositionPlacementSequence surface = ConnectedSurface.create(world, BlockPos.ZERO, Direction.UP, 5, false);

        //5 + 1 = 6, (5 / 2) + 1 = 3
        int x = random.nextInt(6) - 3;
        int z = random.nextInt(6) - 3;
        BlockPos randomPos = new BlockPos(x, 0, z);
        assertEquals(surface.getBoundingBox().contains(randomPos), surface.mayContain(x, 0, z));
    }

    @Test
    void connectedSurfaceShouldOnlyIncludeBlocksThatHaveSameBlockAsStartingPosition5By5RandomSelected30DifferPositions() {
        CasedBlockView world = regionAtOriginWithRandomTargets(5, 30);
        BlockState selectedBlock = world.getBlockState(BlockPos.ZERO);

        for (Direction side : Direction.values()) {
            IPositionPlacementSequence surface = ConnectedSurface.create(world, BlockPos.ZERO, side, 5, false);
            Set<BlockPos> calculated = surface.collect(new ObjectOpenHashSet<>());

            for (BlockPos pos : calculated) {
                assertEquals(selectedBlock, world.getBlockState(pos.offset(side.getOpposite())));
            }
        }
    }

    @Test
    void connectedSurfaceResultsShouldOnlyIncludeBlocksThatHasSameAxisValueAsStarted() {
        CasedBlockView world = regionAtOriginWithRandomTargets(5, 30);

        for (Direction side : Direction.values()) {
            IPositionPlacementSequence surface = ConnectedSurface.create(world, BlockPos.ZERO.offset(side), side.getOpposite(), 5, false);
            Set<BlockPos> calculated = surface.collect(new ObjectOpenHashSet<>());

            int expected = VectorUtils.getAxisValue(BlockPos.ZERO.offset(side), side.getAxis());
            for (BlockPos pos : calculated) {
                assertEquals(expected, VectorUtils.getAxisValue(pos, side.getAxis()));
            }
        }
    }

    @Test
    void connectedSurfaceShouldFindAllConnectedBlocksHardcoded() {
        int radius = 3;
        CasedBlockView world = new CasedBlockView(new Region(-radius, 0, -radius, radius, 0, radius), CasedBlockView.base, CasedBlockView.target)
                //Connected
                .setOtherAt(BlockPos.ZERO)
                .setOtherAt(BlockPos.ZERO.north())
                .setOtherAt(BlockPos.ZERO.east())
                .setOtherAt(BlockPos.ZERO.south())
                .setOtherAt(BlockPos.ZERO.west())
                .setOtherAt(BlockPos.ZERO.north().east())
                .setOtherAt(BlockPos.ZERO.east().south())
                .setOtherAt(BlockPos.ZERO.south().west())
                .setOtherAt(BlockPos.ZERO.west().north())
                //Not connected
                .setOtherAt(BlockPos.ZERO.north(3))
                .setOtherAt(BlockPos.ZERO.east(3))
                .setOtherAt(BlockPos.ZERO.south(3))
                .setOtherAt(BlockPos.ZERO.west(3));

        IPositionPlacementSequence surface = ConnectedSurface.create(world, BlockPos.ZERO, Direction.UP, 5, false);
        assertEquals(9, surface.collect().size());
    }

}


