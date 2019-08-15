package com.direwolf20.buildinggadgets.api.building.view;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.exceptions.TemplateException;
import com.direwolf20.buildinggadgets.api.util.DelegatingSpliterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public final class WorldBackedBuildView implements IBuildView {
    private final IBuildContext context;
    private final Region region;
    private final BiPredicate<IBuildContext, BlockPos> predicate;
    private BlockPos translation;

    public static WorldBackedBuildView inWorld(IWorld world, Region region) {
        return create(SimpleBuildContext.builder().build(world), region);
    }

    public static WorldBackedBuildView create(IBuildContext context, Region region) {
        return create(context, region, null);
    }

    public static WorldBackedBuildView create(IBuildContext context, Region region, @Nullable BiPredicate<IBuildContext, BlockPos> predicate) {
        return new WorldBackedBuildView(
                Objects.requireNonNull(context, "Cannot create WorldBackedBuildView without an IBuildContext!"),
                Objects.requireNonNull(region, "Cannot create WorldBackedBuildView without an Region!"),
                predicate != null ? predicate : (c, p) -> true);
    }

    private WorldBackedBuildView(IBuildContext context, Region region, BiPredicate<IBuildContext, BlockPos> predicate) {
        this.context = context;
        this.region = region;
        this.predicate = predicate;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return new WorldBackedSpliterator(getBoundingBox().spliterator());
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
    public IBuildView copy() {
        return new WorldBackedBuildView(getContext(), getBoundingBox(), predicate);
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
        return region.mayContain(x, y, z) && predicate.test(getContext(), new BlockPos(x, y, z));
    }

    private final class WorldBackedSpliterator extends DelegatingSpliterator<BlockPos, PlacementTarget> {
        private WorldBackedSpliterator(Spliterator<BlockPos> other) {
            super(other);
        }

        @Override
        protected boolean advance(BlockPos object, Consumer<? super PlacementTarget> action) {
            BlockData data = TileSupport.createBlockData(getContext().getWorld(), object);
            action.accept(new PlacementTarget(object.add(translation), data));
            return false;
        }

        @Override
        @Nullable
        public Spliterator<PlacementTarget> trySplit() {
            Spliterator<BlockPos> other = getOther().trySplit();
            if (other != null)
                return new WorldBackedSpliterator(other);
            return null;
        }
    }
}
