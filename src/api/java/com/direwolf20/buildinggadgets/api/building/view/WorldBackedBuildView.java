package com.direwolf20.buildinggadgets.api.building.view;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.util.DelegatingSpliterator;
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
 * calling {@link #evaluate()} (which is equivalent to calling {@code MapBackedBuildView.ofIterable(view.getContext(), view)})
 * to evaluate all {@link BlockData} instances described by this {@link IBuildView view}.
 * <p>
 * Notice that closing has no effect on this object, and therefore also isn't required.
 */
public final class WorldBackedBuildView implements IBuildView {
    private final IBuildContext context;
    private final Region region;
    private final BiFunction<IBuildContext, BlockPos, Optional<BlockData>> dataFactory;
    private BlockPos translation;

    public static WorldBackedBuildView inWorld(IWorld world, Region region) {
        return create(SimpleBuildContext.builder().build(world), region);
    }

    public static WorldBackedBuildView create(IBuildContext context, Region region) {
        return create(context, region, null);
    }

    public static WorldBackedBuildView create(IBuildContext context, Region region, @Nullable BiFunction<IBuildContext, BlockPos, Optional<BlockData>> dataFactory) {
        return new WorldBackedBuildView(
                Objects.requireNonNull(context, "Cannot create WorldBackedBuildView without an IBuildContext!"),
                Objects.requireNonNull(region, "Cannot create WorldBackedBuildView without an Region!"),
                dataFactory != null ? dataFactory : (c, p) -> Optional.of(TileSupport.createBlockData(c.getWorld(), p)));
    }

    private WorldBackedBuildView(IBuildContext context, Region region, BiFunction<IBuildContext, BlockPos, Optional<BlockData>> dataFactory) {
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
    public WorldBackedBuildView translateTo(BlockPos pos) {
        this.translation = pos;
        return this;
    }

    @Override
    public int estimateSize() {
        return region.size();
    }

    @Override
    public void close() {

    }

    @Override
    public WorldBackedBuildView copy() {
        return new WorldBackedBuildView(getContext(), getBoundingBox(), dataFactory);
    }

    public MapBackedBuildView evaluate() {
        return MapBackedBuildView.ofIterable(getContext(), this);
    }

    @Override
    public IBuildContext getContext() {
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
        private final BiFunction<IBuildContext, BlockPos, Optional<BlockData>> dataFactory;
        private final IBuildContext context;

        private WorldBackedSpliterator(Spliterator<BlockPos> other, BlockPos translation, BiFunction<IBuildContext, BlockPos, Optional<BlockData>> dataFactory, IBuildContext context) {
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
