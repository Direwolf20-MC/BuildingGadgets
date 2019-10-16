package com.direwolf20.buildinggadgets.common.template;

import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.util.exceptions.IllegalTemplateFormatException;
import com.direwolf20.buildinggadgets.common.util.exceptions.IllegalTemplateFormatException.CorruptTemplateStringException;
import com.direwolf20.buildinggadgets.common.util.exceptions.IllegalTemplateFormatException.DataCannotBeWrittenException;
import com.direwolf20.buildinggadgets.common.util.exceptions.IllegalTemplateFormatException.IllegalTemplateNBTException.DataCannotBeReadException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Base64;

public final class TemplateIO {
    private static final Gson GSON = TemplateHeader.appendHeaderSpecification(new GsonBuilder(), false, true).create();
    private TemplateIO() {}

    public static void writeTemplate(Template template, OutputStream stream) throws IllegalTemplateFormatException {
        writeTemplate(template, stream, true);
    }

    public static void writeTemplate(Template template, OutputStream stream, boolean persisted) throws IllegalTemplateFormatException {
        CompoundNBT nbt = template.serialize(persisted);
        try {
            CompressedStreamTools.writeCompressed(nbt, stream);
        } catch (IOException e) {
            throw new DataCannotBeWrittenException(e, nbt);
        }
    }

    /**
     * @see #readTemplate(InputStream, TemplateHeader, boolean)
     */
    @Nullable
    public static Template readTemplate(InputStream stream, @Nullable TemplateHeader header) throws IllegalTemplateFormatException {
        return readTemplate(stream, header, true);
    }

    /**
     * @param stream    the Stream to read from
     * @param persisted whether this was written as persisted.
     * @param header    The TemplateHeader if present. Null otherwise.
     * @return A TemplateItem if the serializer is known. Null if not.
     * @throws IOException if a read error occurs or the read nbt does not match the format written by {@link #writeTemplate(Template, OutputStream, boolean)}
     */
    @Nullable
    public static Template readTemplate(InputStream stream, @Nullable TemplateHeader header, boolean persisted) throws IllegalTemplateFormatException {
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
     * @return A TemplateItem if the serializer is known. Null if not.
     * @throws IllegalTemplateFormatException if a read error occurs or the read nbt does not match the format written by {@link #writeTemplate(Template, OutputStream, boolean)}
     */
    @Nullable
    public static Template readTemplate(CompoundNBT nbt, @Nullable TemplateHeader header, boolean persisted) {
        return Template.deserialize(nbt, header, persisted);
    }

    public static void writeTemplateJson(Template template, OutputStream stream) throws IllegalTemplateFormatException {
        writeTemplateJson(template, stream, null);
    }

    public static void writeTemplateJson(Template template, OutputStream stream, @Nullable IBuildContext context) throws IllegalTemplateFormatException {
        GSON.toJson(TemplateJsonRepresentation.ofTemplate(template, context), new OutputStreamWriter(stream));
    }

    public static String writeTemplateJson(Template template) throws IllegalTemplateFormatException {
        return writeTemplateJson(template, (IBuildContext) null);
    }

    public static String writeTemplateJson(Template template, @Nullable IBuildContext context) throws IllegalTemplateFormatException {
        return GSON.toJson(TemplateJsonRepresentation.ofTemplate(template, context));
    }

    public static Template readTemplateFromJson(String json) throws IllegalTemplateFormatException {
        return GSON.fromJson(json, TemplateJsonRepresentation.class).getTemplate();
    }

    public static Template readTemplateFromJson(InputStream stream) throws IllegalTemplateFormatException {
        return GSON.fromJson(new InputStreamReader(stream), TemplateJsonRepresentation.class).getTemplate();
    }

    private static final class TemplateJsonRepresentation {
        public static TemplateJsonRepresentation ofTemplate(Template template, @Nullable IBuildContext context) throws IllegalTemplateFormatException {
            TemplateHeader header = context != null ?
                    template.getHeaderAndForceMaterials(context) :
                    template.getHeader();
            CompoundNBT nbt = template.serialize(true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                CompressedStreamTools.writeCompressed(nbt, baos);
            } catch (IOException e) {
                throw new DataCannotBeWrittenException(e, nbt);
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

        private Template getTemplate() throws IllegalTemplateFormatException {
            byte[] bytes = Base64.getDecoder().decode(body);
            CompoundNBT nbt;
            try {
                nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new CorruptTemplateStringException(e, body);
            }
            return Template.deserialize(nbt, header, true);
        }
    }
}
