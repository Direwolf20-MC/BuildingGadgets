package com.direwolf20.buildinggadgets.api.exceptions;

import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class IllegalTemplateFormatException extends IOException {
    public IllegalTemplateFormatException() {
    }

    public IllegalTemplateFormatException(String message) {
        super(message);
    }

    public IllegalTemplateFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalTemplateFormatException(Throwable cause) {
        super(cause);
    }

    public static class IllegalTemplateNBTException extends IllegalTemplateFormatException {
        private final CompoundNBT nbt;

        public IllegalTemplateNBTException(CompoundNBT nbt) {
            this.nbt = nbt;
        }

        public IllegalTemplateNBTException(String message, CompoundNBT nbt) {
            super(message);
            this.nbt = nbt;
        }

        public IllegalTemplateNBTException(String message, Throwable cause, CompoundNBT nbt) {
            super(message, cause);
            this.nbt = nbt;
        }

        public IllegalTemplateNBTException(Throwable cause, CompoundNBT nbt) {
            super(cause);
            this.nbt = nbt;
        }

        public CompoundNBT getNbt() {
            return nbt;
        }

        public static class MissingSerializerException extends IllegalTemplateNBTException {
            public MissingSerializerException(CompoundNBT nbt) {
                super("Cannot read Template from nbt without serializer ('" + NBTKeys.KEY_SERIALIZER + "')!", nbt);
            }
        }

        public static class MissingDataException extends IllegalTemplateNBTException {
            public MissingDataException(CompoundNBT nbt) {
                super("Cannot read Template from nbt without template data to read ('" + NBTKeys.KEY_DATA + "')!", nbt);
            }
        }

        public static class DataCannotBeReadException extends IllegalTemplateNBTException {
            public DataCannotBeReadException(Throwable cause) {
                super("Unable to read Template nbt from Stream!", cause, null);
            }
        }
    }

    public static class SerialisationException extends IllegalTemplateFormatException {
        private final ResourceLocation serializer;

        public SerialisationException(ResourceLocation serializer) {
            this.serializer = serializer;
        }

        public SerialisationException(String message, ResourceLocation serializer) {
            super(message);
            this.serializer = serializer;
        }

        public SerialisationException(String message, Throwable cause, ResourceLocation serializer) {
            super(message, cause);
            this.serializer = serializer;
        }

        public SerialisationException(Throwable cause, ResourceLocation serializer) {
            super(cause);
            this.serializer = serializer;
        }

        public ResourceLocation getSerializer() {
            return serializer;
        }

        public static class DataCannotBeWrittenException extends SerialisationException {
            private final CompoundNBT nbt;

            public DataCannotBeWrittenException(Throwable cause, ResourceLocation serializer, CompoundNBT nbt) {
                super("Unable to write Template data to bytes when serialised by " + serializer + "!", cause, serializer);
                this.nbt = nbt;
            }

            public CompoundNBT getNbt() {
                return nbt;
            }
        }

        public static class UnknownSerializerException extends SerialisationException {
            public UnknownSerializerException(ResourceLocation serializer) {
                super("Attempted to use unknown serializer " + serializer + " for deserialization!", serializer);
            }
        }

        public static class CorruptTemplateStringException extends SerialisationException {
            private final String templateString;

            public CorruptTemplateStringException(Throwable cause, ResourceLocation serializer, String templateString) {
                super("Unable to process template string to nbt data! Data is corrupted!", cause, serializer);
                this.templateString = templateString;
            }

            public String getTemplateString() {
                return templateString;
            }
        }
    }

}
