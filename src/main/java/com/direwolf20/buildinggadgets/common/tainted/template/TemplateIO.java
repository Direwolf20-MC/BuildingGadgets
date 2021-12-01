package com.direwolf20.buildinggadgets.common.tainted.template;

import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException.CorruptDataException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException.CorruptJsonException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException.DataCannotBeReadException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateReadException.IllegalNBTDataException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateWriteException;
import com.direwolf20.buildinggadgets.common.util.exceptions.TemplateWriteException.DataCannotBeWrittenException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Base64;

public final class TemplateIO {
    private static final Gson GSON = TemplateHeader.appendHeaderSpecification(new GsonBuilder(), false, true).create();
    private TemplateIO() {}

    public static void writeTemplate(Template template, OutputStream stream) throws TemplateWriteException {
        writeTemplate(template, stream, true);
    }

    public static void writeTemplate(Template template, OutputStream stream, boolean persisted) throws TemplateWriteException {
        CompoundTag nbt = template.serialize(persisted);
        try {
            NbtIo.writeCompressed(nbt, stream);
        } catch (IOException e) {
            throw new DataCannotBeWrittenException(e, nbt);
        }
    }

    /**
     * @see #readTemplate(InputStream, TemplateHeader, boolean)
     */
    public static Template readTemplate(InputStream stream, @Nullable TemplateHeader header) throws TemplateReadException {
        return readTemplate(stream, header, true);
    }

    /**
     * @param stream    the Stream to read from
     * @param persisted whether this was written as persisted.
     * @param header    The TemplateHeader if present. Null otherwise.
     * @return A TemplateItem if the serializer is known. Null if not.
     * @throws IOException if a read error occurs or the read nbt does not match the format written by {@link #writeTemplate(Template, OutputStream, boolean)}
     */
    public static Template readTemplate(InputStream stream, @Nullable TemplateHeader header, boolean persisted) throws TemplateReadException {
        try {
            return readTemplate(NbtIo.readCompressed(stream), header, persisted);
        } catch (IOException e) {
            throw new DataCannotBeReadException(e);
        }
    }

    /**
     * @param nbt       the Compound to read from
     * @param persisted whether this was written as persisted.
     * @param header    The TemplateHeader if present. Null otherwise.
     * @return A TemplateItem if the serializer is known. Null if not.
     */
    public static Template readTemplate(CompoundTag nbt, @Nullable TemplateHeader header, boolean persisted) throws TemplateReadException {
        try {
            return Template.deserialize(nbt, header, persisted);
        } catch (Exception e) {
            throw new IllegalNBTDataException(e, nbt);
        }
    }

    public static void writeTemplateJson(Template template, OutputStream stream) throws TemplateWriteException {
        writeTemplateJson(template, stream, null);
    }

    public static void writeTemplateJson(Template template, OutputStream stream, @Nullable BuildContext context) throws TemplateWriteException {
        GSON.toJson(TemplateJsonRepresentation.ofTemplate(template, context), new OutputStreamWriter(stream));
    }

    public static String writeTemplateJson(Template template) throws TemplateWriteException {
        return writeTemplateJson(template, (BuildContext) null);
    }

    public static String writeTemplateJson(Template template, @Nullable BuildContext context) throws TemplateWriteException {
        return GSON.toJson(TemplateJsonRepresentation.ofTemplate(template, context));
    }

    public static Template readTemplateFromJson(String json) throws TemplateReadException {
        try {
            return GSON.fromJson(json, TemplateJsonRepresentation.class).getTemplate();
        } catch (JsonSyntaxException e) {
            throw new CorruptJsonException(e);
        }
    }

    public static Template readTemplateFromJson(InputStream stream) throws TemplateReadException {
        try {
            return GSON.fromJson(new InputStreamReader(stream), TemplateJsonRepresentation.class).getTemplate();
        } catch (JsonSyntaxException e) {
            throw new CorruptJsonException(e);
        }
    }

    private static final class TemplateJsonRepresentation {
        public static TemplateJsonRepresentation ofTemplate(Template template, @Nullable BuildContext context) throws TemplateWriteException {
            TemplateHeader header = context != null ?
                    template.getHeaderAndForceMaterials(context) :
                    template.getHeader();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeTemplate(template, baos);
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

        private Template getTemplate() throws TemplateReadException {
            try {
                byte[] bytes = Base64.getDecoder().decode(body);
                CompoundTag nbt;
                nbt = NbtIo.readCompressed(new ByteArrayInputStream(bytes));
                return Template.deserialize(nbt, header, true);
            } catch (IOException | NullPointerException e) {
                throw new CorruptDataException(e, body);
            }
        }
    }
}
