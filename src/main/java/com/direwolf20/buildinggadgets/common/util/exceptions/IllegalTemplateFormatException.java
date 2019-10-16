package com.direwolf20.buildinggadgets.common.util.exceptions;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.nbt.CompoundNBT;

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
                super("Cannot read TemplateItem from nbt without serializer ('" + com.direwolf20.buildinggadgets.common.util.ref.NBTKeys.KEY_SERIALIZER + "')!", nbt);
            }
        }

        public static class MissingDataException extends IllegalTemplateNBTException {
            public MissingDataException(CompoundNBT nbt) {
                super("Cannot read TemplateItem from nbt without template data to read ('" + NBTKeys.KEY_DATA + "')!", nbt);
            }
        }

        public static class DataCannotBeReadException extends IllegalTemplateNBTException {
            public DataCannotBeReadException(Throwable cause) {
                super("Unable to read TemplateItem nbt from Stream!", cause, null);
            }
        }
    }

    public static class DataCannotBeWrittenException extends IllegalTemplateFormatException {
        private final CompoundNBT nbt;

        public DataCannotBeWrittenException(Throwable cause, CompoundNBT nbt) {
            super("Unable to write TemplateItem data to bytes!", cause);
            this.nbt = nbt;
        }

        public CompoundNBT getNbt() {
            return nbt;
        }
    }

    public static class CorruptTemplateStringException extends IllegalTemplateFormatException {
        private final String templateString;

        public CorruptTemplateStringException(Throwable cause, String templateString) {
            super("Unable to process template string to nbt data! Data is corrupted!", cause);
            this.templateString = templateString;
        }

        public String getTemplateString() {
            return templateString;
        }
    }

}
