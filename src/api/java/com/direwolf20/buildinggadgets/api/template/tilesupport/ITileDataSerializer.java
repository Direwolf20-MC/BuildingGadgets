package com.direwolf20.buildinggadgets.api.template.tilesupport;

import net.minecraft.nbt.NBTTagCompound;

public interface ITileDataSerializer {
    public NBTTagCompound toNBT();

    public ITileEntityData fromNBT();
}
