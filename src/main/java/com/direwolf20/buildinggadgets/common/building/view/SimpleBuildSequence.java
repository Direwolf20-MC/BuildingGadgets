package com.direwolf20.buildinggadgets.common.building.view;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.IBlockProvider;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.placement.IPositionPlacementSequence;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.util.CommonUtils;
import com.direwolf20.buildinggadgets.common.util.spliterator.MappingSpliterator;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiPredicate;

/**
 * Execution context that uses {@link IPositionPlacementSequence} and {@link IBlockProvider} in combination to filter the
 * unusable positions.
 * <p>
 * Testing is done with the predicate produced with {@link #validatorFactory}. If the predicate returns {@code true},
 * the position will be kept and returned in the iterator. If the predicate returns {@code false} on the other hand, the
 * position will be voided.
 *
 * @implNote Execution context in Strategy Pattern
 */
public class SimpleBuildSequence implements IBuildSequence {
    private final IPositionPlacementSequence positions;
    private IBlockProvider<?> blocks;
    private final IValidatorFactory validatorFactory;
    private BuildContext context;
    private BlockPos start;
    @Nullable
    private MaterialList materials;

    /**
     * @see SimpleBuildSequence#SimpleBuildSequence(IPositionPlacementSequence, IBlockProvider, IValidatorFactory, BuildContext, BlockPos)
     *
     * @param positions     List of Block Positions
     * @param blocks        List of Block Providers
     * @param context       Build Context
     */
    public SimpleBuildSequence(IPositionPlacementSequence positions, IBlockProvider<?> blocks, BuildContext context) {
        this(positions, blocks, (world, stack, player, initial) -> (pos, state) -> true, context, null);
    }

    /**
     * Note that it is assumed that this method will return a block provider which uses the first value returned by
     * {@link #positions} as translation.
     *
     * @param positions        Positions
     * @param blocks           List of blocks
     * @param validatorFactory Creates predicate for determining whether a position should be used or not
     * @param buildContext     Build context
     * @param start            Starting BlockPos
     */
    public SimpleBuildSequence(IPositionPlacementSequence positions, IBlockProvider<?> blocks, IValidatorFactory validatorFactory, BuildContext buildContext, @Nullable BlockPos start) {
        this.positions = positions;
        this.blocks = Objects.requireNonNull(blocks, "Cannot have a SimpleBuildSequence without IBlockProvider!");
        this.validatorFactory = validatorFactory;
        if (buildContext.getBuildingPlayer() == null)
            BuildingGadgets.LOG.warn("Constructing SimpleBuildSequence without a player. This may lead to errors down the line, if the used IValidatorFactory doesn't handle null Players!");
        this.context = buildContext;
        this.start = start;
        this.materials = null;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return new MappingSpliterator<>(getFilteredSequence().spliterator(), pos -> new PlacementTarget(pos, getBlockProvider().at(pos)));
    }

    @Override
    public IBuildSequence translateTo(BlockPos pos) {
        blocks = blocks.translate(pos);
        materials = null;
        return this;
    }

    @Override
    public MaterialList estimateRequiredItems(BuildContext context, @Nullable Vec3d simulatePos) {
        if (materials == null)
            materials = IBuildSequence.super.estimateRequiredItems(context, simulatePos);
        return materials;
    }

    @Override
    public int estimateSize() {
        return getBoundingBox().size();
    }

    @Override
    public IBuildSequence copy() {
        return new SimpleBuildSequence(getPositionSequence(), getBlockProvider(), getValidatorFactory(), context, start);
    }

    @Override
    public Region getBoundingBox() {
        return getPositionSequence().getBoundingBox();
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return getPositionSequence().mayContain(x, y, z);
    }

    /**
     * Wrap raw sequence ({@link #getPositionSequence()}) so that the new spliterator only returns positions passing the
     * test of {@link #getValidatorFactory()} with the given World object.
     *
     * @return {@link Spliterator} that wraps {@code getPositionSequence().spliterator()}
     */
    public IPositionPlacementSequence getFilteredSequence() {
        BiPredicate<BlockPos, BlockData> validator = validatorFactory.createValidatorFor(context.getWorld(), context.getUsedStack(), context.getBuildingPlayer(), start);
        return CommonUtils.validatePositionData(getPositionSequence(), validator, getBlockProvider()::at);
    }

    /**
     * @see IPositionPlacementSequence#collect()
     *
     * @return Filtered Block ImmutableList
     */
    public ImmutableList<BlockPos> collectFilteredSequence() {
        return ImmutableList.copyOf(getFilteredSequence());
    }

    public IPositionPlacementSequence getPositionSequence() {
        return positions;
    }

    public IBlockProvider getBlockProvider() {
        return blocks;
    }

    public IValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

    public BuildContext getContext() {
        return context;
    }
}
