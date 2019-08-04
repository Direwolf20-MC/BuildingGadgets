package com.direwolf20.buildinggadgets.api.exceptions;


public class TemplateViewAlreadyClosedException extends TemplateException {
    public TemplateViewAlreadyClosedException(String message) {
        super(message);
    }

    public TemplateViewAlreadyClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateViewAlreadyClosedException(Throwable cause) {
        super(cause);
    }

    public TemplateViewAlreadyClosedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
