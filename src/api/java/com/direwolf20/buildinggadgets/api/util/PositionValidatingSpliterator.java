package com.direwolf20.buildinggadgets.api.util;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import net.minecraft.util.math.BlockPos;

import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

final class PositionValidatingSpliterator extends DelegatingSpliterator<BlockPos, BlockPos> {
    private final BiPredicate<BlockPos, BlockData> predicate;
    private final Function<BlockPos, BlockData> dataExtractor;

    public PositionValidatingSpliterator(Spliterator<BlockPos> other, BiPredicate<BlockPos, BlockData> predicate, Function<BlockPos, BlockData> dataExtractor) {
        super(other);
        this.predicate = predicate;
        this.dataExtractor = dataExtractor;
    }

    @Override
    public boolean advance(BlockPos object, Consumer<? super BlockPos> action) {
        BlockData data = dataExtractor.apply(object);
        if (predicate.test(object, data)) {
            action.accept(object);
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<BlockPos> trySplit() {
        return new PositionValidatingSpliterator(getOther(), predicate, dataExtractor);
    }
}
