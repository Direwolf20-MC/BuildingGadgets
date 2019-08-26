package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.Registries;
import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException;
import com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class TemplateIO {
    private TemplateIO() {}

    public static void writeTemplate(ITemplate template, OutputStream stream) throws IOException {
        writeTemplate(template, stream, true);
    }

    public static void writeTemplate(ITemplate template, OutputStream stream, boolean persisted) throws IOException {
        ITemplateSerializer serializer = template.getSerializer();
        Preconditions.checkArgument(serializer.getRegistryName() != null, "Cannot serialize with a not-registered Serializer!");
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(NBTKeys.KEY_SERIALIZER, serializer.getRegistryName().toString());
        nbt.put(NBTKeys.KEY_DATA, serializer.serialize(template, persisted));
        CompressedStreamTools.writeCompressed(nbt, stream);
    }

    /**
     * @see #readTemplate(InputStream, TemplateHeader, boolean)
     */
    @Nullable
    public static ITemplate readTemplate(InputStream stream, @Nullable TemplateHeader header) throws IOException {
        return readTemplate(stream, header, true);
    }

    /**
     * @param stream    the Stream to read from
     * @param persisted whether this was written as persisted.
     * @param header    The TemplateHeader if present. Null otherwise.
     * @return A Template if the serializer is known. Null if not.
     * @throws IOException if a read error occurs or the read nbt does not match the format written by {@link #writeTemplate(ITemplate, OutputStream, boolean)}
     */
    @Nullable
    public static ITemplate readTemplate(InputStream stream, @Nullable TemplateHeader header, boolean persisted) throws IOException {
        CompoundNBT nbt = CompressedStreamTools.readCompressed(stream);
        if (! nbt.contains(NBTKeys.KEY_SERIALIZER, NBT.TAG_STRING))
            throw new IllegalTemplateFormatException("Cannot read Template without a Serializer!");
        ITemplateSerializer serializer = Registries.getTemplateSerializer(nbt.getString(NBTKeys.KEY_SERIALIZER));
        if (serializer == null)
            return null;
        if (! nbt.contains(NBTKeys.KEY_DATA, NBT.TAG_COMPOUND))
            throw new IllegalTemplateFormatException("Cannot read Template without Template Data!");
        return serializer.deserialize(nbt.getCompound(NBTKeys.KEY_DATA), header, persisted);
    }

    public static void writeTemplateHeaderJson(ITemplate template, OutputStream stream) throws IOException {

    }

    public static TemplateHeader readTemplateHeaderJson(InputStream stream) throws IOException {
        return null;
    }
}
