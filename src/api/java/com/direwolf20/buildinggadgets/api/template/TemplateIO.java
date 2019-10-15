package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.Registries;
import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException;
import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException.IllegalTemplateNBTException.DataCannotBeReadException;
import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException.IllegalTemplateNBTException.MissingDataException;
import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException.IllegalTemplateNBTException.MissingSerializerException;
import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException.SerialisationException.CorruptTemplateStringException;
import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException.SerialisationException.DataCannotBeWrittenException;
import com.direwolf20.buildinggadgets.api.exceptions.IllegalTemplateFormatException.SerialisationException.UnknownSerializerException;
import com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Base64;

public final class TemplateIO {
    private static final Gson GSON = TemplateHeader.appendHeaderSpecification(new GsonBuilder(), false, true).create();
    private TemplateIO() {}

    public static void writeTemplate(ITemplate template, OutputStream stream) throws IllegalTemplateFormatException {
        writeTemplate(template, stream, true);
    }

    public static void writeTemplate(ITemplate template, OutputStream stream, boolean persisted) throws IllegalTemplateFormatException {
        CompoundNBT nbt = new CompoundNBT();
        writeTemplate(template, nbt, persisted);
        try {
            CompressedStreamTools.writeCompressed(nbt, stream);
        } catch (IOException e) {
            throw new DataCannotBeWrittenException(e, template.getSerializer().getRegistryName(), nbt);
        }
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
    public static ITemplate readTemplate(InputStream stream, @Nullable TemplateHeader header) throws IllegalTemplateFormatException {
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
    public static ITemplate readTemplate(InputStream stream, @Nullable TemplateHeader header, boolean persisted) throws IllegalTemplateFormatException {
        try {
            return readTemplate(CompressedStreamTools.readCompressed(stream), header, persisted);
        } catch (IOException e) {
            throw new DataCannotBeReadException(e);
        }
    }

    /**
     * @param nbt       the Compound to read from
     * @param persisted whether this was written as persisted.
     * @param header    The TemplateHeader if present. Null otherwise.
     * @return A Template if the serializer is known. Null if not.
     * @throws IllegalTemplateFormatException if a read error occurs or the read nbt does not match the format written by {@link #writeTemplate(ITemplate, OutputStream, boolean)}
     */
    @Nullable
    public static ITemplate readTemplate(CompoundNBT nbt, @Nullable TemplateHeader header, boolean persisted) throws IllegalTemplateFormatException {
        if (! nbt.contains(NBTKeys.KEY_SERIALIZER, NBT.TAG_STRING))
            throw new MissingSerializerException(nbt);
        ITemplateSerializer serializer = Registries.getTemplateSerializer(nbt.getString(NBTKeys.KEY_SERIALIZER));
        if (serializer == null)
            return null;
        if (! nbt.contains(NBTKeys.KEY_DATA, NBT.TAG_COMPOUND))
            throw new MissingDataException(nbt);
        return serializer.deserialize(nbt.getCompound(NBTKeys.KEY_DATA), header, persisted);
    }

    public static void writeTemplateJson(ITemplate template, OutputStream stream) throws IllegalTemplateFormatException {
        writeTemplateJson(template, stream, null);
    }

    public static void writeTemplateJson(ITemplate template, OutputStream stream, @Nullable IBuildOpenOptions openOptions) throws IllegalTemplateFormatException {
        GSON.toJson(TemplateJsonRepresentation.ofTemplate(template, openOptions), new OutputStreamWriter(stream));
    }

    public static String writeTemplateJson(ITemplate template) throws IllegalTemplateFormatException {
        return writeTemplateJson(template, (IBuildOpenOptions) null);
    }

    public static String writeTemplateJson(ITemplate template, @Nullable IBuildOpenOptions openOptions) throws IllegalTemplateFormatException {
        return GSON.toJson(TemplateJsonRepresentation.ofTemplate(template, openOptions));
    }

    public static ITemplate readTemplateFromJson(String json) throws IllegalTemplateFormatException {
        return GSON.fromJson(json, TemplateJsonRepresentation.class).getTemplate();
    }

    public static ITemplate readTemplateFromJson(InputStream stream) throws IllegalTemplateFormatException {
        return GSON.fromJson(new InputStreamReader(stream), TemplateJsonRepresentation.class).getTemplate();
    }

    private static final class TemplateJsonRepresentation {
        public static TemplateJsonRepresentation ofTemplate(ITemplate template, @Nullable IBuildOpenOptions openOptions) throws IllegalTemplateFormatException {
            ITemplateSerializer serializer = template.getSerializer();
            TemplateHeader header = openOptions != null ?
                    serializer.createHeaderAndTryForceMaterials(template, openOptions) :
                    serializer.createHeaderFor(template);
            CompoundNBT nbt = serializer.serialize(template, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                CompressedStreamTools.writeCompressed(nbt, baos);
            } catch (IOException e) {
                throw new DataCannotBeWrittenException(e, serializer.getRegistryName(), nbt);
            }
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new TemplateJsonRepresentation(header, base64);
        }

        private final TemplateHeader header;
        private final String body;

        private TemplateJsonRepresentation(TemplateHeader header, String body) {
            this.header = header;
            this.body = body;
        }

        private TemplateHeader getHeader() {
            return header;
        }

        private String getBody() {
            return body;
        }

        private ITemplate getTemplate() throws IllegalTemplateFormatException {
            byte[] bytes = Base64.getDecoder().decode(body);
            CompoundNBT nbt;
            try {
                nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new CorruptTemplateStringException(e, header.getSerializer(), body);
            }
            ITemplateSerializer serializer = Registries.getTemplateSerializer(header.getSerializer());
            if (serializer == null)
                throw new UnknownSerializerException(header.getSerializer());
            return serializer.deserialize(nbt, header, true);
        }
    }
}
