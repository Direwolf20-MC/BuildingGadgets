package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.util.tools.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Surface that limits its attempt to blocks that are connected through either on its sides or corners. Candidates are
 * selected from a wall region centered at a point and filtered with a 8-way adjacent flood fill.
 *
 * @see IPositionPlacementSequence#iterator()
 */
public final class ConnectedSurface {
    private ConnectedSurface() {
    }

    /**
     * @param world           Block access for searching reference
     * @param searchingCenter Center of the searching region
     * @param side            Facing to offset from the {@code searchingCenter} to get to the reference region center
     * @param range           The range of the tool
     * @param fuzzy           If the gadget is fuzzy or not
     *
     * @return IPositionPlacementSequence
     */
    public static IPositionPlacementSequence create(Region searchingRegion, IBlockReader world, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
        return create(searchingRegion, world, pos -> pos.offset(side), searchingCenter, side, range, fuzzy);
    }

    public static IPositionPlacementSequence create(Region searchingRegion, IBlockReader world, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
        return create(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
    }

    public static IPositionPlacementSequence create(IBlockReader world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, boolean fuzzy) {
        return new ConnectedSurfaceSequence(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
    }

    public static IPositionPlacementSequence create(IBlockReader world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, BiPredicate<BlockState, BlockPos> predicate) {
        return new ConnectedSurfaceSequence(world, searchingRegion, searching2referenceMapper, searchingCenter, side, predicate);
    }
}