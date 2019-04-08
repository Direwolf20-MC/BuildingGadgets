package com.direwolf20.buildinggadgets.api.template.building;

import com.direwolf20.buildinggadgets.api.abstraction.UniqueItem;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.google.common.collect.Multiset;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A "snapshot" view of a specific {@link ITemplate} providing the ability to iterate over the represented {@link PlacementTarget}'s.
 * It also allows for translating to a specific position via {@link #translateTo(BlockPos)}.<br>
 * Furthermore an {@code ITemplateView} should provide a hint for users to check the amount of blocks an {@link ITemplateView} of this {@code ITemplate} is going to
 * produce at most via {@link #estimateSize()} in combination with hinting the amount of {@link UniqueItem}'s required.
 * However this is not strictly necessary and when computation might be costly it is not advised to return an accurate value.
 * <p>
 * The {@code ITemplateView} is constructed given an instance of {@link IBuildContext} in {@link ITemplate#createViewInContext(IBuildContext)}. This
 * context allows the {@link ITemplateView} to adapt itself to the environment in which it is viewed. Therefore no assumptions may be made, that
 * 2 distinct instances of {@code ITemplateView} will produce the same results even if they were constructed by the same {@link ITemplate} and {@link IBuildContext}.
 * @implSpec Notice that no guarantees are made for the order in which {@link PlacementTarget}'s are produced by this {@code ITemplateView}.
 *         Order may be arbitrary or sorted, consult the documentation of the implementation you are currently faced with for information about traversal order.
 */
public interface ITemplateView extends Iterable<PlacementTarget>, AutoCloseable {
    // TODO add Boundingbox (Region) as soon as @hnOsmium's PR comes in
    /**
     * Creates a {@link Stream} backed by the {@link #spliterator()} of this {@code ITemplateView}.
     * @return A {@link Stream} representing all positions produced by this {@code ITemplateView}.
     * @throws UnsupportedOperationException if {@link #close()}  was called and concurrent Transaction execution is not permitted
     *         by the underlying {@link ITemplate}
     * @implSpec The {@link Stream} produced by this method must not be parallel. A user wishing for parallel traversal
     *         should take a look at {@link java.util.stream.StreamSupport#stream(Spliterator, boolean)}.
     * @implNote The default implementation is equivalent to calling {@code StreamSupport.stream(view.spliterator(), false);}.
     */
    default Stream<PlacementTarget> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * @return An {@link Iterator} over the {@link PlacementTarget}'s of this {@code ITemplateView}.
     * @throws UnsupportedOperationException if {@link #close()} was called and concurrent Transaction execution is not permitted
     *         by the underlying {@link ITemplate}
     * @implNote The default implementation is equivalent to calling {@code Spliterators.iterator(view.spliterator());}.
     */
    @Override
    default Iterator<PlacementTarget> iterator() {
        return Spliterators.iterator(spliterator());
    }

    /**
     * @return An {@link Spliterator} over the {@link PlacementTarget}'s of this {@code ITemplateView}.
     * @throws UnsupportedOperationException if {@link #close()} was called and concurrent Transaction execution is not permitted
     *         by the underlying {@link ITemplate}
     */
    @Override
    Spliterator<PlacementTarget> spliterator();

    /**
     * Translates this {@code ITemplateView} to the specified position.
     * @param pos The position to translate to. May not be null.
     * @return The new translated {@code ITemplateView}. May be the same or a new instance depending on implementation.
     * @throws NullPointerException if the given Position was null
     * @implSpec This Method may not accumulate multiple translations, but instead always set the absolute Translation performed
     *         to the specified value.
     */
    ITemplateView translateTo(BlockPos pos);

    /**
     * Attempts to compute the amount of required {@link UniqueItem}'s. Should never be more than might be needed,
     * but may be fewer if exact requirements are hard or expensive to compute.
     * @return A {@link Multiset} representing the Item Requirements to build this {@code ITemplateView}.
     *         Will be null if unknown or expensive to compute.
     */
    @Nullable
    Multiset<UniqueItem> estimateRequiredItems();

    /**
     * Attempts to compute an estimate of how many {@link PlacementTarget}'s this {@code ITemplateView} will produce.
     * Should never be smaller than the amount produced by iterating over this, but may be larger if an exact size
     * is hard or expensive to compute.
     * <p>
     * A negative value indicates that the size cannot be determined easily.
     * @return A prediction of how many {@link PlacementTarget} this {@code ITemplate} is going to produce.
     *         Negative if unknown or expensive to compute.
     */
    int estimateSize();

    /**
     * Calling this Method will invalidate this {@code TemplateView}. Invalidation both frees this {@code TemplateViews} execution lock on any
     * {@link com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction} and prevents further iteration over this views content if the
     * backing {@link ITemplate} does not support concurrent {@link com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction} execution.
     * Any calls to {@link #iterator()}, {@link #spliterator()} or {@link #stream()} will throw an {@link UnsupportedOperationException} in this case.
     */
    @Override
    void close() throws Exception;
}
