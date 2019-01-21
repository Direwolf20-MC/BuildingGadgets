package com.direwolf20.buildinggadgets.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.UUID;

public interface ITemplateDataStorage {
    public void saveData(@Nonnull UUID uuid, @Nonnull NBTTagCompound compound, @Nonnull World world);

    public NBTTagCompound loadData(@Nonnull UUID uuid, @Nonnull World world);
}
