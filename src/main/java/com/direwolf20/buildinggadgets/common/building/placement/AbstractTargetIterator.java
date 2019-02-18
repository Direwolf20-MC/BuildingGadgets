package com.direwolf20.buildinggadgets.common.building.placement;

import com.google.common.collect.AbstractIterator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Optional predefined iterator logic for implementations of {@link IPlacementSequence}.
 * <p>
 * Note that this base class does validation at the same time, which is not recommended to be used in combination of {@link Context}.
 * In most cases, using the iterator provided by {@link com.direwolf20.buildinggadgets.common.building.Region} is sufficient.
 * </p>
 *
 * @implNote this class reinvented the wheel of {@link AbstractIterator} since it does not expose enough space for further expansion
 */
public abstract class AbstractTargetIterator implements Iterator<BlockPos> {

    private enum State {
        READY,
        NOT_READY,
        DONE
    }

    private MutableBlockPos next;
    private State state = State.NOT_READY;

    public AbstractTargetIterator() {

    }

    /**
     * @see AbstractIterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        switch (state) {
            case READY:
                return true;
            case DONE:
                return false;
        }
        computeNext();
        return state != State.DONE;
    }

    /**
     * @see AbstractIterator#next()
     */
    @Override
    @Nonnull
    public BlockPos next() {
        return nextInternal().toImmutable();
    }

    /**
     * Alternative for {@link #next()} that returns a direct reference to the internal variable.
     *
     * @see #next()
     */
    @NoBorrow
    @Nonnull
    MutableBlockPos poll() {
        return nextInternal();
    }

    @NoBorrow
    @Nonnull
    private MutableBlockPos nextInternal() {
        if (state == State.DONE) {
            throw new NoSuchElementException();
        }
        if (state == State.READY) {
            state = State.NOT_READY;
            return next;
        }
        computeNext();
        return next;
    }

    /**
     * @implNote always return with state == READY or DONE.
     */
    private void computeNext() {
        //When enters with state == DONE, it will skip this loop
        while (true) {
            state = State.NOT_READY;
            //Only possibility is set state to READY or do nothing
            this.attempt();
            //attempt() did not yield anything
            if (state == State.NOT_READY) {
                state = State.DONE;
                break;
            }

            if (validate(next)) {
                state = State.READY;
            }
        }

        //When all positions provided by the implementation are invalid, it will reach here with state == DONE
    }

    /**
     * Advance the iteration once and yield the result.
     *
     * <p>
     * Implementations should call {@link #yield(int, int, int)} or {@link #moveAndYield(EnumFacing, int)} to output the result of this attempt.
     * If there are no elements available, implementation should simply do nothing.
     * </p>
     * <p>When nothing has been done for at least once, this method is guaranteed will not be called again.</p>
     *
     * <p>
     * The result of yielding will be tested against {@link #validate(BlockPos)}.
     * When the validation fails, the method will be re-invoked until either a succeeded validation or {@link #hasNext()} returns false.
     * </p>
     *
     * @see AbstractIterator#computeNext()
     */
    public abstract void attempt();

    protected boolean validate(BlockPos pos) {
        return true;
    }

    /**
     * Yields a position through this method
     */
    protected void yield(int x, int y, int z) {
        next.setPos(x, y, z);
        state = State.READY;
    }

    /**
     * @see #yield(int, int, int)
     */
    protected void moveAndYield(EnumFacing direction, int amount) {
        next.move(direction, amount);
        state = State.READY;
    }

}
