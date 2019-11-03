package com.direwolf20.buildinggadgets.common.building.view;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.template.Template;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

/**
 * A "snapshot" view of a specific buildable TemplateItem providing the ability to iterate over the represented {@link PlacementTarget}'s.
 * It also allows for translating to a specific position via {@link #translateTo(BlockPos)}.<br>
 * Furthermore an {@code IBuildSequence} should provide a hint for users to check the amount of blocks an {@link IBuildSequence} of this {@code TemplateItem} is going to
 * produce at most via {@link #estimateSize()} in combination with hinting the amount of {@link IUniqueObject}'s required.
 * However this is not strictly necessary and when computation might be costly it is not advised to return an accurate value.
 * <p>
 * The {@code IBuildSequence} is constructed given an instance of {@link BuildContext}. This
 * context allows the {@link IBuildSequence} to adapt itself to the environment in which it is viewed. Therefore no assumptions may be made, that
 * 2 distinct instances of {@code IBuildSequence} will produce the same results even if they were constructed by the same {@link BuildContext}.
 * <p>
 * All Methods in this class may throw an {@link IllegalStateException} if called after the {@code IBuildSequence} has been closed.
 * @implSpec Notice that no guarantees are made for the order in which {@link PlacementTarget}'s are produced by this {@code IBuildSequence}.
 * Order may be arbitrary or sorted, consult the documentation of the implementation you are currently faced with for information about traversal order.
 */
public interface IBuildSequence extends IPlacementSequence<PlacementTarget> {
    /**
     * Creates a {@link Stream} backed by the {@link #spliterator()} of this {@code IBuildSequence}.
     *
     * @return A {@link Stream} representing all positions produced by this {@code IBuildSequence}.
     * @implSpec The {@link Stream} produced by this method must not be parallel. A user wishing for parallel traversal
     * should take a look at {@link java.util.stream.StreamSupport#stream(Spliterator, boolean)}.
     * @implNote The default implementation is equivalent to calling {@code StreamSupport.stream(view.spliterator(), false);}.
     * @see IPlacementSequence#stream()
     */
    default Stream<PlacementTarget> stream() {
        return IPlacementSequence.super.stream();
    }

    /**
     * @return An {@link Iterator} over the {@link PlacementTarget}'s of this {@code IBuildSequence}.
     * @implNote The default implementation is equivalent to calling {@code Spliterators.iterator(view.spliterator());}.
     */
    @Override
    default Iterator<PlacementTarget> iterator() {
        return Spliterators.iterator(spliterator());
    }

    /**
     * @return An {@link Spliterator} over the {@link PlacementTarget}'s of this {@code IBuildSequence}.
     */
    @Override
    Spliterator<PlacementTarget> spliterator();

    /**
     * Translates this {@code IBuildSequence} to the specified position.
     *
     * @param pos The position to translate to. May not be null.
     * @return The new translated {@code IBuildSequence}. May be the same or a new instance depending on implementation.
     * @throws NullPointerException if the given Position was null
     * @implSpec This Method may not accumulate multiple translations, but instead always set the absolute Translation performed
     * to the specified value.
     */
    IBuildSequence translateTo(BlockPos pos);

    /**
     * Attempts to compute the amount of required {@link IUniqueObject}'s. Should never be more than might be needed,
     * but may be fewer if exact requirements are hard or expensive to compute.
     *
     * @param simulatePos nullable BlockPos used to simulate
     * @return A {@link MaterialList} representing the Item Requirements to build this {@code IBuildSequence}.
     */
    default MaterialList estimateRequiredItems(BuildContext context, @Nullable Vec3d simulatePos) {
        return CommonUtils.estimateRequiredItems(this, context, simulatePos);
    }

    default MaterialList estimateRequiredItems(BuildContext context) {
        PlayerEntity player = context.getBuildingPlayer();
        return estimateRequiredItems(context, player != null ? player.getPositionVec().add(0, player.getEyeHeight(), 0) : null);
    }

    /**
     * Attempts to compute an estimate of how many {@link PlacementTarget}'s this {@code IBuildSequence} will produce.
     * Should never be smaller than the amount produced by iterating over this, but may be larger if an exact size
     * is hard or expensive to compute.
     * <p>
     * A negative value indicates that the size cannot be determined easily.
     *
     * @return A prediction of how many {@link PlacementTarget} this {@code TemplateItem} is going to produce.
     * Negative if unknown or expensive to compute.
     */
    int estimateSize();

    /**
     * Performs a deep copy of this {@code TemplateView} iterating over all positions if necessary. The resulting {@code TemplateView} should not care about
     * the behaviour of the backing {@link Template} and instead be independent of any resource lock's this {@code TemplateView} imposes as well as not imposing any
     * resource locks itself.
     * <p>
     * Calling
     * {@code
     * IBuildSequence view = template.createViewInContext(ctx);
     * IBuildSequence copy = view.copy();
     * view.close();
     * }
     * should ensure that the {@link Template} is no longer restricted because of an open {@code TemplateView}, whilst at the same time providing access to all the positions via
     * {@code copy} in the original {@code TemplateView} in a non-lazy manner.
     * <p>
     * <b>However: be warned that this might require O(n) execution time for this Method on some implementations (with n being the total number of
     * {@link PlacementTarget PlacementTargets} produced) and will almost certainly nullify any benefits that the original {@code TemplateView} may have had by
     * using a lazy implementation.</b>
     *
     * @return A full copy of this {@code IBuildSequence}. Iterating over the whole {@code IBuildSequence} if necessary.
     */
    @Override
    IBuildSequence copy();
}
