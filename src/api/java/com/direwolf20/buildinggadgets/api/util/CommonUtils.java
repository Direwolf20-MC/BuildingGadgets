package com.direwolf20.buildinggadgets.api.util;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import java.util.function.Predicate;

public final class CommonUtils {
    private CommonUtils() {}

    public static final Comparator<Vec3i> POSITION_COMPARATOR = Comparator
            .comparingInt(Vec3i::getX)
            .thenComparingInt(Vec3i::getY)
            .thenComparingInt(Vec3i::getZ);

    public static <T> IPlacementSequence<T> filterSequence(IPlacementSequence<T> sequence, Predicate<T> filter) {
        return filterSequence(sequence, Function.identity(), filter);
    }

    public static <T, U> IPlacementSequence<T> filterSequence(IPlacementSequence<U> sequence, Function<U, T> mapper, Predicate<T> filter) {
        return new DelegatingPlacementSequence<T, U>(sequence, mapper) {
            @Override
            public Spliterator<T> spliterator() {
                return new FilterSpliterator<>(super.spliterator(), filter);
            }
        };
    }

    public static <T> IPlacementSequence<T> emptySequence() {
        return new IPlacementSequence<T>() {
            @Override
            public Region getBoundingBox() {
                return new Region(new BlockPos(0, 0, 0));
            }

            @Override
            public boolean mayContain(int x, int y, int z) {
                return false;
            }

            @Override
            public IPlacementSequence<T> copy() {
                return emptySequence();
            }

            @Override
            public Iterator<T> iterator() {
                return Collections.emptyIterator();
            }
        };
    }

    public static IPositionPlacementSequence emptyPositionSequence() {
        return mapToPositionPlacementSequence(emptySequence(), Function.identity());
    }

    public static <T,U> IPlacementSequence<U> map(IPlacementSequence<T> sequence, Function<T,U> mapper) {
        return new DelegatingPlacementSequence<>(sequence, mapper);
    }

    public static IPositionPlacementSequence mapToPositionPlacementSequence(final IPlacementSequence<BlockPos> sequence) {
        return mapToPositionPlacementSequence(sequence, Function.identity());
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

    public static MaterialList estimateRequiredItems(Iterable<PlacementTarget> buildView, IBuildContext context) {
        PlayerEntity player = context.getBuildingPlayer();
        return estimateRequiredItems(buildView, context, player != null ? new Vec3d(player.posX, player.posY, player.posZ) : null);
    }

    public static MaterialList estimateRequiredItems(Iterable<PlacementTarget> buildView, IBuildContext context, @Nullable Vec3d simulatePos) {
        MaterialList.Builder builder = MaterialList.builder();
        for (PlacementTarget placementTarget : buildView) {
            BlockRayTraceResult target = simulatePos != null ? CommonUtils.fakeRayTrace(simulatePos, placementTarget.getPos()) : null;
            builder.addAll(placementTarget.getRequiredItems(context, target).getRequiredItems().asList());
        }
        return builder.build();
    }

    public static ImmutableMap<BlockPos, BlockData> targetsToMap(Iterable<PlacementTarget> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).collect(ImmutableMap.toImmutableMap(PlacementTarget::getPos, PlacementTarget::getData));
    }

    public static <T> Function<T, T> inputIfNonNullFunction(@Nullable T otherRes) {
        return t -> otherRes != null ? otherRes : t;
    }

}
