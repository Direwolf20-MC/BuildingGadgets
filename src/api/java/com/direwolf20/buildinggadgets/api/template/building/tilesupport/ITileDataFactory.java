package com.direwolf20.buildinggadgets.api.template.building.tilesupport;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ITileDataFactory {
    @Nullable
    ITileEntityData createDataFor(TileEntity tileEntity);
}
