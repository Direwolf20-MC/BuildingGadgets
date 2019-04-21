package com.direwolf20.buildinggadgets.api.template.serialisation;

import com.direwolf20.buildinggadgets.api.template.ITemplate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * This class represents a Serializer responsible for converting {@link ITemplate}'s into {@link NBTTagCompound}'s and back. Additionally an {@code ITemplateSerializer}
 * allows for creating an {@link TemplateHeader} for a given {@link ITemplate}. Of course an {@code ITemplateSerializer} can only support deserialization for
 * one specific {@link ITemplate} implementation and therefore all methods in this class are expected to throw {@link IllegalArgumentException} if faced with
 * an implementation, which is not supported by this {@code ITemplateSerializer}.
 * <p>
 * Additionally the {@link #serialize(ITemplate, boolean)} and {@link #deserialize(NBTTagCompound, TemplateHeader, boolean)} methods are passed an boolean flag in order to hint
 * whether the save is going to be persisted. This flag is provided to allow for less bandwidth to be used when sending large {@link ITemplate}'s over the Network.
 */
public interface ITemplateSerializer extends IForgeRegistryEntry<ITemplateSerializer> {
    /**
     * This method creates a {@link TemplateHeader} for the given {@link ITemplate}. Notice that an {@link TemplateHeader} is always expected to at least
     * provide the {@link #getRegistryName()} of the {@code ITemplateSerializer} which created it as well as an {@link net.minecraft.util.math.BlockPos}
     * describing the {@link ITemplate}'s x-y-z-size.
     * @param template The {@link ITemplate} for which an {@link TemplateHeader} is to be created.
     * @return A {@link TemplateHeader} for the specified {@link ITemplate}.
     * @throws IllegalArgumentException If the {@link ITemplate} implementation is not supported by this serializer or
     *         if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    TemplateHeader createHeaderFor(ITemplate template);

    /**
     * Serializes a given {@link ITemplate}. The persisted flag is meant to allow for more efficient usage of a Player's Network capabilities by using for example
     * RegistryId's for serialisation instead of RegistryNames.
     * @param template The {@link ITemplate} to serialize.
     * @param persisted Whether or not this data is meant to be persisted.
     * @return A {@link NBTTagCompound} representing the serialized form of the given {@link ITemplate}. May be empty but <b>not null</b> to
     *         indicate that no data requires serialization.
     * @throws IllegalArgumentException If the {@link ITemplate} implementation is not supported by this serializer or
     *         if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    NBTTagCompound serialize(ITemplate template, boolean persisted);

    /**
     * Deserializes an {@link ITemplate} from the given {@link NBTTagCompound}.
     * @param tagCompound The {@link NBTTagCompound} to deserialize an {@link ITemplate} for.
     * @param header The {@link TemplateHeader} for the corresponding {@link NBTTagCompound} or null if not present.
     * @param persisted Whether or not this data was previously persisted. It can be expected that this flag matches
     *         whatever was passed into {@link #serialize(ITemplate, boolean)} to create the {@link NBTTagCompound}.
     * @return The {@link ITemplate} representing the serialized data in the {@link NBTTagCompound}.
     * @throws IllegalArgumentException If the data does not match the format of this serializer or if a persisted save is attempted to be retrieved non-persistently or vice-versa.
     */
    ITemplate deserialize(NBTTagCompound tagCompound, @Nullable TemplateHeader header, boolean persisted);
}
