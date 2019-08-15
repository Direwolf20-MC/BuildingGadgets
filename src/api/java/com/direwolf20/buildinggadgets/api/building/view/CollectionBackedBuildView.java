package com.direwolf20.buildinggadgets.api.building.view;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.exceptions.TemplateException;
import net.minecraft.util.math.BlockPos;

import java.util.Spliterator;

public final class CollectionBackedBuildView implements IBuildView {
    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return null;
    }

    @Override
    public IBuildView translateTo(BlockPos pos) {
        return null;
    }

    @Override
    public int estimateSize() {
        return 0;
    }

    @Override
    public void close() throws TemplateException {

    }

    @Override
    public IBuildView copy() {
        return null;
    }

    @Override
    public IBuildContext getContext() {
        return null;
    }

    @Override
    public Region getBoundingBox() {
        return null;
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return false;
    }
}
