package com.direwolf20.buildinggadgets.common.util.exceptions;

import net.minecraft.nbt.CompoundNBT;

public class TemplateWriteException extends Exception {
    public TemplateWriteException() {
    }

    public TemplateWriteException(String message) {
        super(message);
    }

    public TemplateWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateWriteException(Throwable cause) {
        super(cause);
    }

    public TemplateWriteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static final class DataCannotBeWrittenException extends TemplateWriteException {
        private final CompoundNBT nbt;

        public DataCannotBeWrittenException(Throwable cause, CompoundNBT nbt) {
            super("Unable to write TemplateItem data to bytes!", cause);
            this.nbt = nbt;
        }

        public CompoundNBT getNbt() {
            return nbt;
        }
    }
}
