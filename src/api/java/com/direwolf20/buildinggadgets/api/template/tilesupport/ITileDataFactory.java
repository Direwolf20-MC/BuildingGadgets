package com.direwolf20.buildinggadgets.api.template.tilesupport;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public interface ITileDataFactory {
    @Nullable
    public ITileEntityData createDataFor(TileEntity tileEntity);
}
