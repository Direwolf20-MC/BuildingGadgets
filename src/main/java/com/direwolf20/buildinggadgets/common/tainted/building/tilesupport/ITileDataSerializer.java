package com.direwolf20.buildinggadgets.common.tainted.building.tilesupport;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * This class represents a serializer responsible for converting {@link ITileEntityData} into {@link CompoundNBT} and back. Of course an {@code ITileDataSerializer}
 * can only support deserialization for one specific {@link ITileEntityData} implementation and therefore all methods in this class are expected
 * to throw {@link IllegalArgumentException} if faced with an implementation, which is not supported by this {@code ITileEntityDataSerializer}.
 * <p>
 * Additionally the {@link #serialize(ITileEntityData, boolean)} and {@link #deserialize(CompoundNBT, boolean)} methods are passed an boolean flag in order to hint
 * whether the save is going to be persisted. This flag is provided to allow for less bandwidth to be used when sending large {@link ITileEntityData}'s over the Network.
 */
public interface ITileDataSerializer extends IForgeRegistryEntry<ITileDataSerializer> {
    /**
     * Serializes a given {@link ITileEntityData}. The persisted flag is meant to allow for more efficient usage of a Player's Network capabilities by using for example
     * RegistryId's for serialisation instead of RegistryNames.
     * @param data The {@link ITileEntityData} to serialize.
     * @param persisted Whether or not this data is meant to be persisted.
     * @return A {@link CompoundNBT} representing the serialized form of the given {@link ITileEntityData}. May be empty but <b>not null</b> to
     *         indicate that no data requires serialization.
     * @throws IllegalArgumentException If the {@link ITileEntityData} implementation is not supported by this serializer or
     *         if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    CompoundNBT serialize(ITileEntityData data, boolean persisted);

    /**
     * Deserializes an {@link ITileEntityData} from the given {@link CompoundNBT}.
     * @param tagCompound The {@link CompoundNBT} to deserialize an {@link ITileEntityData} for.
     * @param persisted Whether or not this data was previously persisted. It can be expected that this flag matches
     *         whatever was passed into {@link #serialize(ITileEntityData, boolean)} to create the {@link CompoundNBT}.
     * @return The {@link ITileEntityData} representing the serialized data in the {@link CompoundNBT}.
     * @throws IllegalArgumentException If the data does not match the format of this serializer or if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    ITileEntityData deserialize(CompoundNBT tagCompound, boolean persisted);
}
