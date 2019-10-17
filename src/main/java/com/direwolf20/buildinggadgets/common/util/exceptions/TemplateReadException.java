package com.direwolf20.buildinggadgets.common.util.exceptions;

import net.minecraft.nbt.CompoundNBT;

public class TemplateReadException extends Exception {
    public TemplateReadException() {
    }

    public TemplateReadException(String message) {
        super(message);
    }

    public TemplateReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateReadException(Throwable cause) {
        super(cause);
    }

    public TemplateReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static final class CorruptJsonException extends TemplateReadException {
        public CorruptJsonException() {
            super("Failed to read Template, because given copy does not constitute a valid Template-json.");
        }

        public CorruptJsonException(Throwable cause) {
            super("Failed to read Template, because given copy does not constitute a valid Template-json.", cause);
        }
    }

    public static final class CorruptDataException extends TemplateReadException {
        private final String templateData;

        public CorruptDataException(String templateData) {
            super("Could not interpret Template body as a valid Template.");
            this.templateData = templateData;
        }

        public CorruptDataException(Throwable cause, String templateData) {
            super("Could not interpret Template body as a valid Template.", cause);
            this.templateData = templateData;
        }

        public String getTemplateData() {
            return templateData;
        }
    }

    public static class DataCannotBeReadException extends TemplateReadException {
        public DataCannotBeReadException(Throwable cause) {
            super("Unable to read TemplateItem nbt from Stream!", cause);
        }
    }

    public static class IllegalNBTDataException extends TemplateReadException {
        private final CompoundNBT nbt;

        public IllegalNBTDataException(CompoundNBT nbt) {
            super("Could not read nbt data format.");
            this.nbt = nbt;
        }

        public IllegalNBTDataException(Throwable cause, CompoundNBT nbt) {
            super("Could not read nbt data format.", cause);
            this.nbt = nbt;
        }

        public CompoundNBT getNbt() {
            return nbt;
        }
    }
}
