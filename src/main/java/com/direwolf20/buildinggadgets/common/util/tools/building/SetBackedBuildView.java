package com.direwolf20.buildinggadgets.common.util.tools.building;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.util.MappingSpliterator;
import net.minecraft.util.math.BlockPos;

import java.util.Set;
import java.util.Spliterator;

public class SetBackedBuildView implements IBuildView {
    private final Region bounds;
    private final Set<PlacementTarget> targets;
    private final IBuildContext context;
    private BlockPos translation;

    public SetBackedBuildView(IBuildContext context, Set<PlacementTarget> targets, Region bounds) {
        this.bounds = bounds;
        this.targets = targets;
        this.context = context;
        this.translation = BlockPos.ZERO;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return new MappingSpliterator<>(targets.spliterator(), t -> new PlacementTarget(t.getPos().add(translation), t.getData()));
    }

    @Override
    public SetBackedBuildView translateTo(BlockPos pos) {
        this.translation = pos;
        return this;
    }

    @Override
    public int estimateSize() {
        return targets.size();
    }

    @Override
    public void close() {

    }

    @Override
    public SetBackedBuildView copy() {
        return new SetBackedBuildView(context, targets, bounds).translateTo(translation);
    }

    @Override
    public IBuildContext getContext() {
        return context;
    }

    @Override
    public Region getBoundingBox() {
        return bounds;
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return bounds.mayContain(x, y, z);
    }
}
