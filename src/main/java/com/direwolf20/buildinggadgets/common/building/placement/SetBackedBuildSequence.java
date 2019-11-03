package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.building.view.IBuildSequence;
import com.direwolf20.buildinggadgets.common.util.spliterator.MappingSpliterator;
import net.minecraft.util.math.BlockPos;

import java.util.Set;
import java.util.Spliterator;

public class SetBackedBuildSequence implements IBuildSequence {
    private final Region bounds;
    private final Set<PlacementTarget> targets;
    private BlockPos translation;

    public SetBackedBuildSequence(Set<PlacementTarget> targets, Region bounds) {
        this.bounds = bounds;
        this.targets = targets;
        this.translation = BlockPos.ZERO;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return new MappingSpliterator<>(targets.spliterator(), t -> new PlacementTarget(t.getPos().add(translation), t.getData()));
    }

    @Override
    public SetBackedBuildSequence translateTo(BlockPos pos) {
        this.translation = pos;
        return this;
    }

    @Override
    public int estimateSize() {
        return targets.size();
    }

    @Override
    public SetBackedBuildSequence copy() {
        return new SetBackedBuildSequence(targets, bounds).translateTo(translation);
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
