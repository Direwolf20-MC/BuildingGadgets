package com.direwolf20.buildinggadgets.api.building.placement;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.util.MathUtils;
import com.direwolf20.buildinggadgets.api.util.VectorUtils;
import com.google.common.base.Preconditions;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
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
     * Column is a line of blocks that is aligned to some axis, starting from a position to another where 2 and only 2 coordinates
     * are the same. Whether the resulting {@link BlockPos BlockPositions} include the start/end position is up to the factory methods'
     * specification.
     */
    public static final class Column {
        private Column() {
        }

        /**
         * Construct a column object with a starting point, including {@code range} amount of elements.
         *
         * @param hit   the source position, will not be included
         * @param side  side to grow the column into
         * @param range length of the column
         * @implSpec this sequence includes the source position
         *
         * @return {@link IPositionPlacementSequence}
         */
        public static IPositionPlacementSequence extendFrom(BlockPos hit, Direction side, int range) {
            return new Region(hit, hit.offset(side, range - 1));
        }

        /**
         * Construct a column object of the specified length, centered at a point and aligned to the given axis.
         *
         * @param center center of the column
         * @param axis   which axis will the column align to
         * @param length length of the column, will be floored to an odd number if it is not one already
         *
         * @return {@link com.direwolf20.buildinggadgets.api.building.IPlacementSequence}
         */
        public static IPositionPlacementSequence centerAt(BlockPos center, Axis axis, int length) {
            Direction positive = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis);
            Direction negative = positive.getOpposite();
            BlockPos base = center.offset(negative, (length - 1) / 2);
            // -1 because Region's vertexes are inclusive
            return new Region(base, base.offset(positive, MathUtils.floorToOdd(length) - 1));
        }

        /**
         * Starts from the selected position, and extend a column of blocks towards a target position on the axis of the selected face.
         *
         * @param source         Source Block
         * @param target         Target Block
         * @param axis           which axis will the column align to
         * @param maxProgression Max range / Progression
         *
         * @return {@link IPositionPlacementSequence}
         */
        public static IPositionPlacementSequence createAxisChasing(BlockPos source, BlockPos target, Axis axis, int maxProgression) {
            int difference = VectorUtils.getAxisValue(target, axis) - VectorUtils.getAxisValue(source, axis);
            if (difference < 0)
                return createAxisChasing(source, target, Direction.getFacingFromAxis(AxisDirection.NEGATIVE, axis), maxProgression);
            return createAxisChasing(source, target, Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis), maxProgression);
        }

        /**
         * <p>Note that this factory method does not verify that {@code offsetDirection} is appropriate.</p>
         *
         * @param source          Source Block
         * @param target          Target Block
         * @param offsetDirection The direction offset into
         * @param maxProgression  Max range / Progression
         *
         * @return IPositionPlacementSequence
         */
        public static IPositionPlacementSequence createAxisChasing(BlockPos source, BlockPos target, Direction offsetDirection, int maxProgression) {
            Axis axis = offsetDirection.getAxis();
            int difference = VectorUtils.getAxisValue(target, axis) - VectorUtils.getAxisValue(source, axis);
            maxProgression = Math.min(Math.abs(difference), maxProgression);

            return extendFrom(source, offsetDirection, maxProgression);
        }
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

        /**
         * Creates a wall extending to some direction with the given position as its bottom.
         *
         * @param posHit    bottom of the wall
         * @param extension top side (growing direction) of the wall
         * @param flatSide  front face of the wall
         * @param radius    radius of the wall.
         * @param extra     amount of blocks to add beyond the radius
         *
         * @return {@link IPositionPlacementSequence}
         */
        public static IPositionPlacementSequence extendingFrom(BlockPos posHit, Direction extension, Direction flatSide, int radius, int extra) {
            Preconditions.checkArgument(extension != flatSide, "Cannot have a wall extending to " + extension + " and flat at " + flatSide);
            return create(posHit.offset(extension, radius + 1), flatSide, radius, extension, extra);
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
     * Grid is a set of blocks where each block is equidistant from its neighboring blocks. The distance between the blocks
     * is a periodic sequence with a certain size.
     */
    public static final class Grid {
        private Grid() {
        }

        public static IPositionPlacementSequence create(BlockPos base, int range, int periodSize) {
            return new GridSequence(base, range, periodSize);
        }
    }

    /**
     * A sequence of blocks that offsets in 2 different directions where one is vertical, one is horizontal.
     * <p>
     * For example, a regular climbing up stair facing north would have (UP, NORTH) as its parameter. This also applies to
     * descending stair like (DOWN, SOUTH) where each block is lower than the latter.
     */
    public static final class Stair {
        private Stair() {
        }

        public static IPositionPlacementSequence create(BlockPos base, Direction horizontalAdvance, Direction verticalAdvance, int range) {
            return new StairSequence(base, horizontalAdvance, verticalAdvance, range);
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
         * @return {@link com.direwolf20.buildinggadgets.api.building.IPlacementSequence}
         */
        public static IPositionPlacementSequence create(IBlockReader world, BlockPos searchingCenter, Direction side, int range, boolean fuzzy) {
            Region searchingRegion = Wall.clickedSide(searchingCenter, side, range / 2).getBoundingBox();
            return create(world, searchingCenter, searchingRegion, pos -> pos.offset(side), fuzzy);
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
            Region searchingRegion = Wall.clickedSide(searchingCenter, side, range / 2).getBoundingBox();
            return create(world, searchingRegion, pos -> pos.offset(side), searchingCenter, side, fuzzy);
        }

        public static IPositionPlacementSequence create(IBlockReader world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, boolean fuzzy) {
            return new ConnectedSurfaceSequence(world, searchingRegion, searching2referenceMapper, searchingCenter, side, fuzzy);
        }

        public static IPositionPlacementSequence create(IBlockReader world, Region searchingRegion, Function<BlockPos, BlockPos> searching2referenceMapper, BlockPos searchingCenter, @Nullable Direction side, BiPredicate<BlockState, BlockPos> predicate) {
            return new ConnectedSurfaceSequence(world, searchingRegion, searching2referenceMapper, searchingCenter, side, predicate);
        }
    }
}
