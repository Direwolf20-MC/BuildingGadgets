package com.direwolf20.buildinggadgets.api.exceptions;

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
}
