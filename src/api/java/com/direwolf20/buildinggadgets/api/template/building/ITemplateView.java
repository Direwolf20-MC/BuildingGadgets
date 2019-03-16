package com.direwolf20.buildinggadgets.api.template.building;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ITemplateView extends Iterable<PlacementTarget> {
    /**
     * Creates a {@link Stream} backed by the {@link #spliterator()} of this {@code ITemplate}.
     * @return A {@link Stream} representing all positions produced by this {@code ITemplate}.
     * @implSpec The {@link Stream} produced by this method must not be parallel. A user wishing for parallel traversal
     * should take a look at {@link java.util.stream.StreamSupport#stream(Spliterator, boolean)}.
     */
    default Stream<PlacementTarget> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    default Iterator<PlacementTarget> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    Spliterator<PlacementTarget> spliterator();

    /**
     * Translates this {@code ITemplate} to the specified position.
     * @param pos The position to translate to. May not be null.
     * @return The new translated {@link ITemplate}. May be the same or a new instance depending on implementation.
     * @throws NullPointerException if the given Position was null
     * @implSpec This Method may not accumulate multiple translations, but instead always set the absolute Translation performed
     * to the specified value.
     */
    ITemplate translateTo(BlockPos pos);
}
