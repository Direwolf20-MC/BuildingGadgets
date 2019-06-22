package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITileDataSerializer;
import com.direwolf20.buildinggadgets.api.template.serialisation.SerialisationSupport;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public enum DummyTileEntityData implements ITileEntityData {
    INSTANCE;

    @Override
    public ITileDataSerializer getSerializer() {
        return SerialisationSupport.dummyDataSerializer();
    }

    @Override
    public boolean placeIn(IBuildContext context, BlockState state, BlockPos position) {
        return context.getWorld().setBlockState(position, state, 0);
    }
}
