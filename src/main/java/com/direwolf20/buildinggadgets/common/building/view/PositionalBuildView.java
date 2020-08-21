package com.direwolf20.buildinggadgets.common.building.view;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.util.spliterator.MappingSpliterator;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * A simple {@link IBuildView} backed by a {@link Map Map<BlockPos, BlockData>}. {@link PlacementTarget PlacementTargets} will be created
 * lazily when iterating over this {@link IBuildView}. You can supply this with a mutable {@link Map} via {@link #createUnsafe(BuildContext, Map, Region)}
 * for efficiency reasons, note however that you will encounter undefined behaviour if the {@link Map} is modified after this {@link IBuildView} was
 * created.
 */
public final class PositionalBuildView implements IBuildView {
    private Map<BlockPos, BlockData> map;
    private Region boundingBox;
    private BlockPos translation;
    private BuildContext context;


    public static PositionalBuildView createUnsafe(BuildContext context, Map<BlockPos, BlockData> map, Region boundingBox) {
        return new PositionalBuildView(
                Objects.requireNonNull(context, "Cannot have a PositionalBuildView without BuildContext!"),
                Objects.requireNonNull(map, "Cannot have a PositionalBuildView without position to data map!"),
                Objects.requireNonNull(boundingBox, "Cannot have a PositionalBuildView without a boundingBox!")
        );
    }

    private PositionalBuildView(BuildContext context, Map<BlockPos, BlockData> map, Region boundingBox) {
        this.context = context;
        this.map = map;
        this.boundingBox = boundingBox;
        this.translation = BlockPos.ZERO;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        BlockPos translation = this.translation;
        return new MappingSpliterator<>(map.entrySet().spliterator(), e -> new PlacementTarget(e.getKey().add(translation), e.getValue()));
    }

    @Override
    public PositionalBuildView translateTo(BlockPos pos) {
        boundingBox = boundingBox.translate(pos.subtract(translation));//translate the bounding box to the correct position
        this.translation = pos;
        return this;
    }

    @Override
    public int estimateSize() {
        return map.size();
    }


    @Override
    public PositionalBuildView copy() {
        return new PositionalBuildView(context, map, boundingBox);
    }

    @Override
    public Region getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public BuildContext getContext() {
        return context;
    }

    public ImmutableMap<BlockPos, BlockData> getMap() {
        return ImmutableMap.copyOf(map);
    }
}
