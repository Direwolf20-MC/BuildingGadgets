package com.direwolf20.buildinggadgets.common.util.spliterator;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public final class PositionValidatingSpliterator extends DelegatingSpliterator<BlockPos, BlockPos> {
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

    @Nullable
    @Override
    public Spliterator<BlockPos> trySplit() {
        Spliterator<BlockPos> split = getOther().trySplit();
        if (split != null)
            return new PositionValidatingSpliterator(split, predicate, dataExtractor);
        return null;
    }
}
