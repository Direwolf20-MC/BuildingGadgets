package com.direwolf20.buildinggadgets.api.util;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class CommonUtils {
    private CommonUtils() {}

    public static final Comparator<Vec3i> POSITION_COMPARATOR = Comparator
            .comparingInt(Vec3i::getX)
            .thenComparingInt(Vec3i::getY)
            .thenComparingInt(Vec3i::getZ);

    public static <T,U> IPlacementSequence<U> map(IPlacementSequence<T> sequence, Function<T,U> mapper) {
        return new IPlacementSequence<U>() {
            @Override
            public Spliterator<U> spliterator() {
                return new MappingSpliterator<>(sequence.spliterator(), mapper);
            }

            @Override
            public Iterator<U> iterator() {
                return Spliterators.iterator(spliterator());
            }

            @Override
            public IPlacementSequence<U> copy() {
                return map(sequence,mapper);
            }

            @Override
            public Region getBoundingBox() {
                return sequence.getBoundingBox();
            }

            @Override
            public boolean mayContain(int x, int y, int z) {
                return sequence.mayContain(x,y,z);
            }
        };
    }

    public static <T> IPositionPlacementSequence mapToPositionPlacementSequence(final IPlacementSequence<T> sequence, final Function<T, BlockPos> positionGenerator) {
        final IPlacementSequence<BlockPos> mapped = map(sequence,positionGenerator);
        return new IPositionPlacementSequence() {

            @Override
            public Spliterator<BlockPos> spliterator() {
                return mapped.spliterator();
            }

            @Override
            public Iterator<BlockPos> iterator() {
                return Spliterators.iterator(spliterator());
            }

            @Override
            public IPositionPlacementSequence copy() {
                return mapToPositionPlacementSequence(sequence, positionGenerator);
            }

            @Override
            public Region getBoundingBox() {
                return sequence.getBoundingBox();
            }

            @Override
            public boolean mayContain(int x, int y, int z) {
                return sequence.mayContain(x,y,z);
            }
        };
    }

    public static IPositionPlacementSequence validatePositionData(final IPositionPlacementSequence sequence, BiPredicate<BlockPos, BlockData> predicate, Function<BlockPos, BlockData> dataExtractor) {
        return new IPositionPlacementSequence() {
            @Override
            public Spliterator<BlockPos> spliterator() {
                return new PositionValidatingSpliterator(sequence.spliterator(), predicate, dataExtractor);
            }

            @Override
            public Iterator<BlockPos> iterator() {
                return Spliterators.iterator(spliterator());
            }

            @Override
            public IPositionPlacementSequence copy() {
                return validatePositionData(sequence, predicate, dataExtractor);
            }

            @Override
            public Region getBoundingBox() {
                return sequence.getBoundingBox();
            }

            @Override
            public boolean mayContain(int x, int y, int z) {
                return sequence.mayContain(x, y, z);
            }
        };
    }

    public static BlockRayTraceResult fakeRayTrace(Vec3d simulatePos, BlockPos pos) {
        return fakeRayTrace(simulatePos.getX(), simulatePos.getY(), simulatePos.getZ(), pos);
    }

    public static BlockRayTraceResult fakeRayTrace(double simX, double simY, double simZ, BlockPos pos) {
        Vec3d simVec = new Vec3d(pos).subtract(simX, simY, simZ);
        Direction dir = Direction.getFacingFromVector(simVec.getX(), simVec.getY(), simVec.getZ());
        return new BlockRayTraceResult(simVec, dir, pos, false);
    }

}
