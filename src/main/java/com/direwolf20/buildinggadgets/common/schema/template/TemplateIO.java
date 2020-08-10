package com.direwolf20.buildinggadgets.common.schema.template;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public final class TemplateIO {
    public static Optional<byte[]> getCompressedBytes(Template template) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            CompressedStreamTools.writeCompressed(template.serializeNBT(), baos);
            return Optional.of(baos.toByteArray());
        } catch (IOException e) {
            BuildingGadgets.LOGGER.error("Unable to write Template {} to bytes!!!", template, e);
            return Optional.empty();
        }
    }
}
