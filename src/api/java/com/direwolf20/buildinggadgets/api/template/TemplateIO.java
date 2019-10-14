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
import java.io.*;

public final class TemplateIO {
    private TemplateIO() {}

    public static void writeTemplate(ITemplate template, OutputStream stream) throws IOException {
        writeTemplate(template, stream, true);
    }

    public static void writeTemplate(ITemplate template, OutputStream stream, boolean persisted) throws IOException {
        CompoundNBT nbt = new CompoundNBT();
        writeTemplate(template, nbt, persisted);
        CompressedStreamTools.writeCompressed(nbt, stream);
    }

    public static void writeTemplate(ITemplate template, CompoundNBT nbt, boolean persisted) {
        ITemplateSerializer serializer = template.getSerializer();
        Preconditions.checkArgument(serializer.getRegistryName() != null, "Cannot serialize with a not-registered Serializer!");
        nbt.putString(NBTKeys.KEY_SERIALIZER, serializer.getRegistryName().toString());
        nbt.put(NBTKeys.KEY_DATA, serializer.serialize(template, persisted));
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
        return readTemplate(CompressedStreamTools.readCompressed(stream), header, persisted);
    }

    /**
     * @param nbt       the Compound to read from
     * @param persisted whether this was written as persisted.
     * @param header    The TemplateHeader if present. Null otherwise.
     * @return A Template if the serializer is known. Null if not.
     * @throws IOException if a read error occurs or the read nbt does not match the format written by {@link #writeTemplate(ITemplate, OutputStream, boolean)}
     */
    @Nullable
    public static ITemplate readTemplate(CompoundNBT nbt, @Nullable TemplateHeader header, boolean persisted) throws IllegalTemplateFormatException {
        if (! nbt.contains(NBTKeys.KEY_SERIALIZER, NBT.TAG_STRING))
            throw new IllegalTemplateFormatException("Cannot read Template without a Serializer!");
        ITemplateSerializer serializer = Registries.getTemplateSerializer(nbt.getString(NBTKeys.KEY_SERIALIZER));
        if (serializer == null)
            return null;
        if (! nbt.contains(NBTKeys.KEY_DATA, NBT.TAG_COMPOUND))
            throw new IllegalTemplateFormatException("Cannot read Template without Template Data!");
        return serializer.deserialize(nbt.getCompound(NBTKeys.KEY_DATA), header, persisted);
    }

    public static void writeTemplateJson(ITemplate template, OutputStream stream) throws IOException {
        String json = writeTemplateJson(template);
        new OutputStreamWriter(stream).append(json);
    }

    public static String writeTemplateJson(ITemplate template) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static ITemplate readTemplateFromJson(String json) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static ITemplate readTemplateFromJson(InputStream stream) throws IOException {
        Reader r = new InputStreamReader(stream);
        StringBuilder builder = new StringBuilder();
        char[] buf = new char[4096];
        int read = 0;
        while ((read = r.read(buf)) > 0) {
            builder.append(buf, 0, read);
        }
        return readTemplateFromJson(builder.toString());
    }
}
