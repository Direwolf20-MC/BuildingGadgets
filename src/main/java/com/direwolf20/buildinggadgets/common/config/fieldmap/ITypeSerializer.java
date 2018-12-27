package com.direwolf20.buildinggadgets.common.config.fieldmap;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;

public interface ITypeSerializer {
    @Nullable
    public NBTBase serializeValue(FieldWrapper field) throws IllegalAccessException;

    public boolean applyValue(NBTBase tag, FieldWrapper field) throws IllegalAccessException;
}
