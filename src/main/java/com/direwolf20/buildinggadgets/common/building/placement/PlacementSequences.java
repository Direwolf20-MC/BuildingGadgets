package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.util.tools.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class PlacementSequences {

    private PlacementSequences() {
    }

    /**
     * A wall is a plane of blocks described with a starting and an ending position. The positions will and must have 1
     * coordinate that is the same.
     * <p>
     * See the static factory methods for more information.
     */
    public static final class Wall {
        private Wall() {
        }

        /**
         * Creates a wall centered at the given position.
         *
         * @param center the center of the wall
         * @param side   front face of the wall
         * @param radius radius of the wall
         *
         * @return {@link IPositionPlacementSequence}
         */
        public static IPositionPlacementSequence clickedSide(BlockPos center, Direction side, int radius) {
            return create(center, side, radius, null, 0);
        }

        private static IPositionPlacementSequence create(BlockPos posHit, Direction side, int radius, @Nullable Direction extendingSide, int extendingSize) {
            Region createdRegion = new Region(posHit).expand(
                    radius * (1 - Math.abs(side.getXOffset())),
                    radius * (1 - Math.abs(side.getYOffset())),
                    radius * (1 - Math.abs(side.getZOffset())));

            if (extendingSize != 0 && extendingSide != null) {
                if (extendingSide.getAxisDirection() == AxisDirection.POSITIVE)
                    createdRegion = new Region(createdRegion.getMin(), createdRegion.getMax().offset(extendingSide, extendingSize));
                else
                    createdRegion = new Region(createdRegion.getMin().offset(extendingSide, extendingSize), createdRegion.getMax());
            }
            return createdRegion;
        }
    }

    /**
     * Surface mode where no connectivity is required. All blocks within the region (wall centered at some position) will
     * be selected if it fulfills the requirement -- has its underside same as the starting position.
     *
     * @see ConnectedSurface
     */
    public static final class Surface {
        private Surface() {}

        /**
         * @param world           Block access for searching reference
         * @param searchingCenter Center of the searching region
         * @param side            Facing to offset from the {@code searchingCenter} to get to the reference region center
         * @param range           Range of the gadget
         * @param fuzzy           If the gadget is fuzzy
         *
         * @return {@link com.direwolf20.buildinggadgets.common.building.IPlacementSequence}
         */
        public static IPositionPlacementSequence create(IBlockReader world, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
            return create(world, pos -> pos.offset(side), searchingCenter, side, range, fuzzy);
        }

        public static IPositionPlacementSequence create(IBlockReader world, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
            Region searchingRegion = Wall.clickedSide(searchingCenter, side, MathUtils.floorToOdd(range) / 2).getBoundingBox();
            return create(world, searchingCenter, searchingRegion, searching2referenceMapper, fuzzy);
        }

        public static IPositionPlacementSequence create(IBlockReader world, BlockPos center, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, boolean fuzzy) {
            return new SurfaceSequence(Objects.requireNonNull(world, "Surface mode is required to have at least an IBlockReader"),
                    Objects.requireNonNull(center),
                    Objects.requireNonNull(searchingRegion),
                    Objects.requireNonNull(searching2referenceMapper),
                    fuzzy);
        }
    }

    /**
     * Surface that limits its attempt to blocks that are connected through either on its sides or corners. Candidates are
     * selected from a wall region centered at a point and filtered with a 8-way adjacent flood fill.
     *
     * @see IPositionPlacementSequence#iterator()
     * @see Surface
     */
    public static final class ConnectedSurface {
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
        public static IPositionPlacementSequence create(IBlockReader world, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
            return create(world, pos -> pos.offset(side), searchingCenter, side, range, fuzzy);
        }

        public static IPositionPlacementSequence create(IBlockReader world, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
            Region searchingRegion = Wall.clickedSide(searchingCenter, side, MathUtils.floorToOdd(range) / 2).getBoundingBox();
            return create(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
        }

        public static IPositionPlacementSequence create(IBlockReader world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, boolean fuzzy) {
            return new ConnectedSurfaceSequence(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
        }

        public static IPositionPlacementSequence create(IBlockReader world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, BiPredicate<BlockState, BlockPos> predicate) {
            return new ConnectedSurfaceSequence(world, searchingRegion, searching2referenceMapper, searchingCenter, side, predicate);
        }
    }
}
