package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.building.ITemplateView;
import com.direwolf20.buildinggadgets.api.template.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.google.common.collect.Multiset;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

/**
 * Instances of this class represent a 3D-Template for any kind of structure in the Form of an {@link Iterable} of {@link PlacementTarget}'s.
 * An {@code ITemplate} therefore has the following responsibilities:
 * <ul>
 *     <li>Provide a possibility to iterate over all placement information contained in this {@code ITemplate}.</li>
 *     <li>Provide a boundingBox, which will enclose all positions produced by this {@code ITemplate}.</li>
 *     <li>Provide a possibility to evaluate a Set of {@link net.minecraft.item.Item}'s required by a player to build this {@code ITemplate}.</li>
 *     <li>Provide a possibility to translate to a given position so that all {@link PlacementTarget}'s represented by this
 *         {@code ITemplate} will be offset by the specified amount.</li>
 *     <li>Optionally an {@code ITemplate} may choose to provide a possibility for modifying the represented structure via an {@link ITemplateTransaction}.</li>
 * </ul>
 * <p>
 * Furthermore an {@code ITemplate} should provide a hint for users to check the amount of blocks this {@code ITemplate} is going to produce at most via {@link #estimateSize()}.
 * However this is not strictly necessary and when computation might be costly it is not advised to return an accurate value, if any value at all. <br>
 * Here is a small example of how to iterate over all non-Air Blocks. Of course if a world is available the alternative {@link net.minecraft.block.state.IBlockState#isAir(IBlockReader, BlockPos)}
 * should be used... <br>
 * {@code ITemplate template = ...;
 *        template.stream().filter(t -> !t.getData().getState().isAir()).forEach(t -> System.out.println("Non Air block found at "+t.getPos()+"!"));
 * }
 * </p>
 * @implSpec Notice that it is not a responsibility of this class to handle placement or modification in any way.
 * @implSpec Notice further that no guarantees are made for the order in which {@link PlacementTarget}'s are produced by this {@code ITemplate}.
 *           Order may be arbitrary or sorted, consult the documentation of the implementation you are currently faced with for information about traversal order.
 */
public interface ITemplate {
    //TODO add Boundingbox (Region) as soon as @hnOsmium's PR comes in

    ITemplateSerializer getSerializer();

    ITemplateView createViewInContext(IBuildContext buildContext);


    /**
     * Creates a new {@link ITemplateTransaction} for modifying this {@code ITemplate}. The created {@link ITemplateTransaction}
     * will only modify modify this {@code ITemplate} when {@link ITemplateTransaction#execute()} is called.
     * Therefore iteration on this {@code ITemplate} must still be permitted even when an {@link ITemplateTransaction} has been created.
     * It is upon the {@link ITemplateTransaction} to fail if multiple {@link ITemplateTransaction} attempt to execute in parallel or
     * this {@code ITemplate} is currently iterated upon.
     * <p>
     * An implementation is not required to support modification via an {@link ITemplateTransaction}. As a result this method may
     * return null if it is not supported. Furthermore an implementation may choose not to support multiple {@link ITemplateTransaction}'s at the same
     * time and therefore return null if an {@link ITemplateTransaction} has been created, but not yet been executed.
     * </p>
     * @return A new {@link ITemplateTransaction} for this {@code ITemplate} or null if not supported.
     */
    @Nullable
    ITemplateTransaction startTransaction();


    /**
     * @return A {@link Multiset} representing the Item Requirements to build this {@code ITemplate}
     */
    Multiset<IUniqueItem> estimateRequiredItems();

    /**
     * Attempts to compute an estimate of how many {@link PlacementTarget}'s this {@code ITemplate} will produce.
     * Should never be smaller than the amount produced by iterating over this, but may be larger if an exact size
     * is hard or expensive to compute.<br>
     * A negative value indicates that the size cannot be determined easily.
     * @return A prediction of how many {@link PlacementTarget} this {@code ITemplate} is going to produce.
     *         Negative if unknown of expensive to compute
     */
    int estimateSize();

}
