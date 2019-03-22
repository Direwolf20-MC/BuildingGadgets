package com.direwolf20.buildinggadgets.api.template.building;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A "snapshot" view of a specific {@link ITemplate} providing the ability to iterate over the represented {@link PlacementTarget}'s.
 * It also allows for translating to a specific position via {@link #translateTo(BlockPos)}.
 * <p>
 * The {@code ITemplateView} is constructed given an instance of {@link IBuildContext} in {@link ITemplate#createViewInContext(IBuildContext)}. This
 * context allows the {@link ITemplateView} to adapt itself to the environment in which it is viewed. Therefore no assumptions may be made, that
 * 2 distinct instances of {@code ITemplateView} will produce the same results even if they were constructed by the same {@link ITemplate}.
 * @implSpec Notice that no guarantees are made for the order in which {@link PlacementTarget}'s are produced by this {@code ITemplateView}.
 *         Order may be arbitrary or sorted, consult the documentation of the implementation you are currently faced with for information about traversal order.
 */
public interface ITemplateView extends Iterable<PlacementTarget> {
    /**
     * Creates a {@link Stream} backed by the {@link #spliterator()} of this {@code ITemplateView}.
     * @return A {@link Stream} representing all positions produced by this {@code ITemplateView}.
     * @implSpec The {@link Stream} produced by this method must not be parallel. A user wishing for parallel traversal
     *         should take a look at {@link java.util.stream.StreamSupport#stream(Spliterator, boolean)}.
     * @implNote The default implementation is equivalent to calling {@code StreamSupport.stream(view.spliterator(), false);}.
     */
    default Stream<PlacementTarget> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * @return An {@link Iterator} over the {@link PlacementTarget}'s of this {@code ITemplateView}.
     * @implNote The default implementation is equivalent to calling {@code Spliterators.iterator(view.spliterator());}.
     */
    @Override
    default Iterator<PlacementTarget> iterator() {
        return Spliterators.iterator(spliterator());
    }

    /**
     * @return An {@link Spliterator} over the {@link PlacementTarget}'s of this {@code ITemplateView}.
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
}
