package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.google.common.collect.Multiset;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Instances of this class represent a 3D-Template for any kind of structure in the Form of an {@link Iterable} of {@link PlacementTarget}'s.
 * An Template therefore has the following responsibilities:
 * <ul>
 *     <li>Provide a possibility to iterate over all Placement information contained in this {@code ITemplate}.</li>
 *     <li>Provide a boundingBox, which will enclose all Positions produced by this {@code ITemplate}.</li>
 *     <li>Provide a possibility to evaluate a Set of {@link net.minecraft.item.Item}'s required by a player to build this {@code ITemplate}.</li>
 *     <li>Provide a possibility to translate to a given position so that all {@link PlacementTarget}'s represented by this
 *         {@code ITemplate} will be offset by the specified amount.</li>
 *     <li>Optionally an {@code ITemplate} may choose to provide a possibility for modifying the represented structure via an {@link ITemplateTransaction}.</li>
 * </ul><br>
 * Furthermore an {@code ITemplate} should provide a hint for users to check the amount of Blocks this {@code ITemplate} is going to produce at most via {@link #estimateSize()}.
 * However this is not strictly necessary and when computation might be costly it is not advised to return an accurate value, if any value at all. <br>
 * Here a small example of how to iterate over all non-Air Blocks. Of course if a world is available the alternative {@link net.minecraft.block.state.IBlockState#isAir(IBlockReader, BlockPos)}
 * should be used... <br>
 * {@code ITemplate template = ...;
 *        template.stream().filter(t -> !t.getData().getState().isAir()).forEach(t -> System.out.println("Non Air block found at "+t.getPos()+"!"));
 * }
 * @implSpec Notice that it is not a responsibility of this class to handle Placement or modification in any way.
 * @implSpec Notice further that no guarantees are made for the order in which {@link PlacementTarget}'s are produced by this {@code ITemplate}.
 *           Order may be arbitrary or sorted, consult the documentation of the Implementation you are currently faced with for information about that.
 */
public interface ITemplate extends Iterable<PlacementTarget> {
    //TODO add Boundingbox (Region) as soon as @hnOsmium's PR comes in

    /**
     * Creates a Stream backed by the {@link #spliterator()} of this {@code ITemplate}.
     * @return A {@link Stream} representing all Positions produced by this {@code ITemplate}.
     * @implSpec The {@link Stream} produced by this Method must not be parallel. A user wishing for parallel traversal
     *           should take a look at {@link java.util.stream.StreamSupport#stream(Spliterator, boolean)}.
     */
    public Stream<PlacementTarget> stream();

    /**
     * Creates a new {@link ITemplateTransaction} for modifying this {@code ITemplate}. The created {@link ITemplateTransaction}
     * will only modify modify this {@code ITemplate} when {@link ITemplateTransaction#execute(ITemplateTransaction.DuplicateStrategy)} is called.
     * Therefore iteration on this {@code ITemplate} must still be permitted even when an {@link ITemplateTransaction} has been created.
     * It is upon the {@link ITemplateTransaction} to fail if multiple {@link ITemplateTransaction} attempt to execute in parallel or
     * this {@code ITemplate} is currently iterated upon.
     * <br>
     * An implementation is not required to support modification via an {@link ITemplateTransaction}. As a result this Method may
     * return null if it is not supported. Furthermore an implementation may choose not to support multiple {@link ITemplateTransaction}'s at the same
     * Time and therefore return null if an {@link ITemplateTransaction} has been created, but not yet been executed.
     * @return A new {@link ITemplateTransaction} for this {@code ITemplate} or null if not supported
     */
    @Nullable
    public ITemplateTransaction startTransaction();

    /**
     * Translates this {@code ITemplate} to the specified position.
     * @param pos The position to translate to. May not be null.
     * @return The new translated {@link ITemplate}. May be the same or a new instance depending on implementation.
     * @throws NullPointerException if the given Position was null
     * @implSpec This Method may not accumulate multiple translations, but instead always set the absolute Translation performed
     *           to the specified value.
     */
    public ITemplate translateTo(BlockPos pos);

    /**
     * @return A {@link Multiset} representing the Item Requirements to build this {@code ITemplate}
     */
    public Multiset<IUniqueItem> getRequiredItems();

    /**
     * Attempts to compute an estimate of how many {@link PlacementTarget}'s this {@code ITemplate} will produce.
     * Should never be smaller than the amount produced by iterating over this, but may be larger if an exact size
     * is hard or expensive to compute.<br>
     * A negative value indicates that the size cannot be determined easily.
     * @return A prediction of how many {@link PlacementTarget} this {@code ITemplate} is going to produce.
     *         Negative if unknown of expensive to compute
     */
    public int estimateSize();
}
