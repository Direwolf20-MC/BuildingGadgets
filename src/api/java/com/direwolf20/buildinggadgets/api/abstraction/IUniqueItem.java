package com.direwolf20.buildinggadgets.api.abstraction;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface IUniqueItem {
    public ResourceLocation getRegistryName();

    @Nullable
    public NBTTagCompound getTag();
}
