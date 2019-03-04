package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.common.building.Region;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class Single implements IPlacementSequence {

    public static Single create(BlockPos target) {
        return new Single(new Region(target));
    }

    private final Region target;

    public Single(Region target) {
        this.target = target;
    }

    @Override
    public Region getBoundingBox() {
        return target;
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return target.contains(x, y, z);
    }

    @Override
    public IPlacementSequence copy() {
        return new Single(target);
    }

    @Nonnull
    @Override
    public Iterator<BlockPos> iterator() {
        return target.iterator();
    }

}
