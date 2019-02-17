package com.direwolf20.buildinggadgets.common.tools.gadget.placement;

import com.google.common.base.Preconditions;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import java.util.Iterator;

public abstract class AbstractTargetIterator implements Iterator<PlacementTarget> {

    private MutableBlockPos current;

    /**
     * The result of this method should update immediately after a call to {@link #attempt()} that yields the last available element.
     * If this is not fulfilled, unrecoverable errors might occur.
     */
    @Override
    public abstract boolean hasNext();

    @Override
    public PlacementTarget next() {
        return nextInternal(true);
    }

    @NoBorrow
    PlacementTarget poll() {
        return nextInternal(false);
    }

    private PlacementTarget nextInternal(boolean view) {
        Preconditions.checkState(hasNext());

        while (this.hasNext()) {
            this.attempt();
            if (validate(current)) {
                return new PlacementTarget(getBlockProvider().at(current), view ? current.toImmutable() : current);
            }
        }
        //This should never happen
        throw new IllegalStateException("");
    }

    public BlockPos view() {
        return peek().toImmutable();
    }

    /**
     * @implNote checks for null to prevent people call this method before they call {@link #next()} at least once
     */
    @NoBorrow
    BlockPos peek() {
        return Preconditions.checkNotNull(current);
    }

    /**
     * Advance the iteration once and yield the result.
     *
     * <p>
     * When {@link #hasNext()} returns {@code false}, it will be guaranteed that will be no more invocation on this method.
     * </p>
     * <p>
     * Implementations should call {@link #yield(int, int, int)} or {@link #moveAndYield(EnumFacing, int)} to output the result of this attempt.
     * If there are no elements available, implementation should simply do nothing.
     * </p>
     *
     * <p>
     * The result of yielding will be tested against {@link #validate(BlockPos)}.
     * When the validation fails, the method will be re-invoked until either a succeeded validation or {@link #hasNext()} returns false.
     * </p>
     */
    public abstract void attempt();

    protected boolean validate(BlockPos pos) {
        return true;
    }

    /**
     * It is assumed that this method will return a block provider uses the first value returned by {@link #next()} as its origin.
     */
    protected abstract IBlockProvider getBlockProvider();

    protected void yield(int x, int y, int z) {
        current.setPos(x, y, z);
    }

    protected void moveAndYield(EnumFacing direction, int amount) {
        current.move(direction, amount);
    }

}
