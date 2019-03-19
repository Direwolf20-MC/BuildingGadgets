package com.direwolf20.buildinggadgets.api.template.serialisation;

import com.direwolf20.buildinggadgets.api.template.building.tilesupport.ITileEntityData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * <p>
 * This class represents a serializer responsible for converting {@link ITileEntityData} into {@link NBTTagCompound} and back. Of course an {@code ITileDataSerializer}
 * can only support deserialization for one specific {@link ITileEntityData} implementation and therefore all methods in this class are expected
 * to throw {@link IllegalArgumentException} if faced with an implementation, which is not supported by this {@code ITileEntityDataSerializer}.
 * </p>
 * <p>
 * Additionally the {@link #serialize(ITileEntityData, boolean)} and {@link #deserialize(NBTTagCompound, boolean)} methods are passed an boolean flag in order to hint
 * whether the save is going to be persisted. This flag is provided to allow for less bandwidth to be used when sending large {@link ITileEntityData}'s over the Network.
 * </p>
 */
public interface ITileDataSerializer extends IForgeRegistryEntry<ITileDataSerializer> {
    /**
     * <p>
     *     Serializes a given {@link ITileEntityData}. The persisted flag is meant to allow for more efficient usage of a Player's Network capabilities by using for example
     *     RegistryId's for serialisation instead of RegistryNames.
     * </p>
     * @param data The {@link ITileEntityData} to serialize.
     * @param persisted Whether or not this data is meant to be persisted.
     * @return A {@link NBTTagCompound} representing the serialized form of the given {@link ITileEntityData}. May be empty but <b>not null</b> to
     *         indicate that no data requires serialization.
     * @throws IllegalArgumentException If the {@link ITileEntityData} implementation is not supported by this serializer or
     *         if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    NBTTagCompound serialize(ITileEntityData data, boolean persisted);

    /**
     * <p>
     *     Deserializes an {@link ITileEntityData} from the given {@link NBTTagCompound}.
     * </p>
     * @param tagCompound The {@link NBTTagCompound} to deserialize an {@link ITileEntityData} for.
     * @param persisted Whether or not this data was previously persisted. It can be expected that this flag matches
     *        whatever was passed into {@link #serialize(ITileEntityData, boolean)} to create the {@link NBTTagCompound}.
     * @return The {@link ITileEntityData} representing the serialized data in the {@link NBTTagCompound}.
     * @throws IllegalArgumentException If the data does not match the format of this serializer or if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    ITileEntityData deserialize(NBTTagCompound tagCompound, boolean persisted);
}
