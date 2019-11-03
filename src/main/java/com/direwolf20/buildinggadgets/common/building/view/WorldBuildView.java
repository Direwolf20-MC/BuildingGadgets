package com.direwolf20.buildinggadgets.common.building.view;

import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.util.spliterator.DelegatingSpliterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * An {@link IBuildView} which views a {@link Region} in an {@link IWorld} as an {@link IBuildView}. {@link PlacementTarget PlacementTargets}
 * will be created lazily when iterating over this {@link IBuildView} via {@code new PlacementTarget(pos, TileSupport.createBlockData(world, pos))}
 * where pos is the Position currently iterating on and world is the world provided by this views {@link #getContext() build context}.
 * <p>
 * This {@link IBuildView} is especially useful, when trying to read all {@link BlockData} instances with in a given {@link Region}.
 * If you need this Information in a pre-determined way, or intend on iterating multiple times on this {@link IBuildView} consider
 * calling {@link #evaluate()} (which is equivalent to calling {@code PositionalBuildView.ofIterable(view.getContext(), view)})
 * to evaluate all {@link BlockData} instances described by this {@link IBuildView view}.
 */
public final class WorldBuildView implements IBuildView {
    private final BuildContext context;
    private final Region region;
    private final BiFunction<BuildContext, BlockPos, Optional<BlockData>> dataFactory;
    private BlockPos translation;

    public static WorldBuildView inWorld(IWorld world, Region region) {
        return create(BuildContext.builder().build(world), region);
    }

    public static WorldBuildView create(BuildContext context, Region region) {
        return create(context, region, null);
    }

    public static WorldBuildView create(BuildContext context, Region region, @Nullable BiFunction<BuildContext, BlockPos, Optional<BlockData>> dataFactory) {
        return new WorldBuildView(
                Objects.requireNonNull(context, "Cannot create WorldBuildView without an BuildContext!"),
                Objects.requireNonNull(region, "Cannot create WorldBuildView without an Region!"),
                dataFactory != null ? dataFactory : (c, p) -> Optional.of(TileSupport.createBlockData(c.getWorld(), p)));
    }

    private WorldBuildView(BuildContext context, Region region, BiFunction<BuildContext, BlockPos, Optional<BlockData>> dataFactory) {
        this.context = context;
        this.region = region;
        this.dataFactory = dataFactory;
        this.translation = BlockPos.ZERO;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return new WorldBackedSpliterator(getBoundingBox().spliterator(), translation, dataFactory, getContext());
    }

    @Override
    public WorldBuildView translateTo(BlockPos pos) {
        this.translation = pos;
        return this;
    }

    @Override
    public int estimateSize() {
        return region.size();
    }

    @Override
    public WorldBuildView copy() {
        return new WorldBuildView(getContext(), getBoundingBox(), dataFactory);
    }

    public PositionalBuildView evaluate() {
        return PositionalBuildView.ofIterable(getContext(), this);
    }

    @Override
    public BuildContext getContext() {
        return context;
    }

    @Override
    public Region getBoundingBox() {
        return region;
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return region.mayContain(x, y, z) && dataFactory.apply(getContext(), new BlockPos(x, y, z)).isPresent();
    }

    private static final class WorldBackedSpliterator extends DelegatingSpliterator<BlockPos, PlacementTarget> {
        private final BlockPos translation;
        private final BiFunction<BuildContext, BlockPos, Optional<BlockData>> dataFactory;
        private final BuildContext context;

        private WorldBackedSpliterator(Spliterator<BlockPos> other, BlockPos translation, BiFunction<BuildContext, BlockPos, Optional<BlockData>> dataFactory, BuildContext context) {
            super(other);
            this.translation = translation;
            this.dataFactory = dataFactory;
            this.context = context;
        }

        @Override
        protected boolean advance(BlockPos object, Consumer<? super PlacementTarget> action) {
            Optional<BlockData> dataOptional = dataFactory.apply(context, object);
            if (dataOptional.isPresent()) {
                BlockData data = dataOptional.get();
                action.accept(new PlacementTarget(object.add(translation), data));
                return true;
            }
            return false;
        }

        @Override
        public Comparator<? super PlacementTarget> getComparator() {
            return Comparator.comparing(PlacementTarget::getPos);
        }

        @Override
        @Nullable
        public Spliterator<PlacementTarget> trySplit() {
            Spliterator<BlockPos> other = getOther().trySplit();
            if (other != null)
                return new WorldBackedSpliterator(other, translation, dataFactory, context);
            return null;
        }

        @Override
        public int characteristics() {
            return ORDERED | SORTED | DISTINCT;
        }
    }
}
