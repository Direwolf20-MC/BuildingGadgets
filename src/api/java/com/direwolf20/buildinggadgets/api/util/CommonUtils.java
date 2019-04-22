package com.direwolf20.buildinggadgets.api.util;

import com.direwolf20.buildinggadgets.api.abstraction.IPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.google.common.collect.AbstractIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Comparator;
import java.util.Iterator;
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
            public Iterator<U> iterator() {
                return new AbstractIterator<U>() {
                    private final Iterator<T> iterator = sequence.iterator();
                    @Override
                    protected U computeNext() {
                        if (iterator.hasNext())
                            return mapper.apply(iterator.next());
                        return endOfData();
                    }
                };
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
            public Iterator<BlockPos> iterator() {
                return mapped.iterator();
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
}
