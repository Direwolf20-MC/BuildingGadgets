package com.direwolf20.buildinggadgets.building.placementTests;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.ConnectedSurface;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import com.direwolf20.buildinggadgets.util.CasedBlockView;
import com.direwolf20.buildinggadgets.util.UniqueBlockState;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.*;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectedSurfaceTest {

    private final Random random = new Random();
    private final IBlockState base = UniqueBlockState.createNew();
    private final IBlockState target = UniqueBlockState.createNew();

    private CasedBlockView regionAtOriginWithRandomTargets(int size, int amountTargets) {
        int radius = (size - 1) / 2;
        CasedBlockView world = new CasedBlockView(new Region(-radius, 0, -radius, radius, 0, radius), base, target);
        for (Iterator<Integer> it = random.ints(amountTargets * 2, 0, size + 1).iterator(); it.hasNext(); ) {
            int x = it.next() - radius - 1;
            int z = it.next() - radius - 1;
            world.setOtherAt(new BlockPos(x, 0, z));
        }

        return world;
    }

    @Test
    void connectedSurfaceShouldUseFuzzyMayContainsToRegion5By5() {
        CasedBlockView world = new CasedBlockView(new Region(-2, 0, -2, 2, 0, 2), base, target);
        ConnectedSurface surface = ConnectedSurface.create(world, BlockPos.ORIGIN, EnumFacing.UP, 5, false);

        //5 + 1 = 6, (5 / 2) + 1 = 3
        int x = random.nextInt(6) - 3;
        int z = random.nextInt(6) - 3;
        BlockPos randomPos = new BlockPos(x, 0, z);
        assertEquals(surface.getBoundingBox().contains(randomPos), surface.mayContain(x, 0, z));
    }

    @Test
    void connectedSurfaceShouldOnlyIncludeBlocksThatHasSameBlockUnderAsStartingPosition5By5RandomSelected30DifferPositions() {
        CasedBlockView world = regionAtOriginWithRandomTargets(5, 30);
        IBlockState selectedBlock = world.getBlockState(BlockPos.ORIGIN);

        for (EnumFacing side : EnumFacing.VALUES) {
            ConnectedSurface surface = ConnectedSurface.create(world, BlockPos.ORIGIN, side, 5, false);
            Set<BlockPos> calculated = surface.collect(new ObjectOpenHashSet<>());

            for (BlockPos pos : calculated) {
                assertEquals(selectedBlock, world.getBlockState(pos.offset(side.getOpposite())));
            }
        }
    }

    @Test
    void connectedSurfaceResultsShouldOnlyIncludeBlocksThatHasSameAxisValueAsStarted() {
        CasedBlockView world = regionAtOriginWithRandomTargets(5, 30);

        for (EnumFacing side : EnumFacing.VALUES) {
            ConnectedSurface surface = ConnectedSurface.create(world, BlockPos.ORIGIN, side, 5, false);
            Set<BlockPos> calculated = surface.collect(new ObjectOpenHashSet<>());

            int expected = VectorTools.getAxisValue(BlockPos.ORIGIN.offset(side), side.getAxis());
            for (BlockPos pos : calculated) {
                assertEquals(expected, VectorTools.getAxisValue(pos, side.getAxis()));
            }
        }
    }

    @Test
    void connectedSurfaceShouldFindAllConnectedBlocksHardcoded() {
        int radius = 3;
        CasedBlockView world = new CasedBlockView(new Region(-radius, 0, -radius, radius, 0, radius), base, target)
                //Connected
                .setOtherAt(BlockPos.ORIGIN)
                .setOtherAt(BlockPos.ORIGIN.north())
                .setOtherAt(BlockPos.ORIGIN.east())
                .setOtherAt(BlockPos.ORIGIN.south())
                .setOtherAt(BlockPos.ORIGIN.west())
                .setOtherAt(BlockPos.ORIGIN.north().east())
                .setOtherAt(BlockPos.ORIGIN.east().south())
                .setOtherAt(BlockPos.ORIGIN.south().west())
                .setOtherAt(BlockPos.ORIGIN.west().north())
                //Not connected
                .setOtherAt(BlockPos.ORIGIN.north(3))
                .setOtherAt(BlockPos.ORIGIN.east(3))
                .setOtherAt(BlockPos.ORIGIN.south(3))
                .setOtherAt(BlockPos.ORIGIN.west(3));

        ConnectedSurface surface = ConnectedSurface.create(world, BlockPos.ORIGIN, EnumFacing.UP, 5, false);
        assertEquals(9, surface.collect().size());
    }

}


