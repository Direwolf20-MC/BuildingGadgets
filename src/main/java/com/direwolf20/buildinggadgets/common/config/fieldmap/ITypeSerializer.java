package com.direwolf20.buildinggadgets.common.config.fieldmap;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;

/**
 * Interface representing an Object which can serialize some kind of Field(s) into NBTData.
 */
public interface ITypeSerializer {
    /**
     *
     * @param field The Field to serialize
     * @implSpec This Method should return null, if it is incapable of parsing the given Field
     * @return Some kind of NBTValue representing this field, which can later be parsed by this TypeSerializer
     * @throws IllegalAccessException if The Field could not be accessed by the given {@link FieldWrapper}
     */
    @Nullable
    public NBTBase serializeValue(FieldWrapper field) throws IllegalAccessException;

    /**
     *
     * @param tag The Tag's value to apply
     * @param field The field to apply the value to
     * @return whether or not this ITypeSerializer was capable of applying the given value
     * @throws IllegalAccessException if The Field could not be accessed by the given {@link FieldWrapper}
     */
    public boolean applyValue(NBTBase tag, FieldWrapper field) throws IllegalAccessException;
}
