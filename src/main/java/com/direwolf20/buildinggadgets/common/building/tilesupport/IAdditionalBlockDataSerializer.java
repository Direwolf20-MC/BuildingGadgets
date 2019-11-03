package com.direwolf20.buildinggadgets.common.building.tilesupport;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * This class represents a serializer responsible for converting {@link IAdditionalBlockData} into {@link CompoundNBT} and back. Of course an {@code IAdditionalBlockDataSerializer}
 * can only support deserialization for one specific {@link IAdditionalBlockData} implementation and therefore all methods in this class are expected
 * to throw {@link IllegalArgumentException} if faced with an implementation, which is not supported by this {@code ITileEntityDataSerializer}.
 * <p>
 * Additionally the {@link #serialize(IAdditionalBlockData, boolean)} and {@link #deserialize(CompoundNBT, boolean)} methods are passed an boolean flag in order to hint
 * whether the save is going to be persisted. This flag is provided to allow for less bandwidth to be used when sending large {@link IAdditionalBlockData}'s over the Network.
 */
public interface IAdditionalBlockDataSerializer extends IForgeRegistryEntry<IAdditionalBlockDataSerializer> {
    /**
     * Serializes a given {@link IAdditionalBlockData}. The persisted flag is meant to allow for more efficient usage of a Player's Network capabilities by using for example
     * RegistryId's for serialisation instead of RegistryNames.
     * @param data The {@link IAdditionalBlockData} to serialize.
     * @param persisted Whether or not this data is meant to be persisted.
     * @return A {@link CompoundNBT} representing the serialized form of the given {@link IAdditionalBlockData}. May be empty but <b>not null</b> to
     *         indicate that no data requires serialization.
     * @throws IllegalArgumentException If the {@link IAdditionalBlockData} implementation is not supported by this serializer or
     *         if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    CompoundNBT serialize(IAdditionalBlockData data, boolean persisted);

    /**
     * Deserializes an {@link IAdditionalBlockData} from the given {@link CompoundNBT}.
     * @param tagCompound The {@link CompoundNBT} to deserialize an {@link IAdditionalBlockData} for.
     * @param persisted Whether or not this data was previously persisted. It can be expected that this flag matches
     *         whatever was passed into {@link #serialize(IAdditionalBlockData, boolean)} to create the {@link CompoundNBT}.
     * @return The {@link IAdditionalBlockData} representing the serialized data in the {@link CompoundNBT}.
     * @throws IllegalArgumentException If the data does not match the format of this serializer or if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    IAdditionalBlockData deserialize(CompoundNBT tagCompound, boolean persisted);
}
