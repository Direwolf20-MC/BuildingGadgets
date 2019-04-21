package com.direwolf20.buildinggadgets.api.exceptions;

public class TransactionInvalidException extends TransactionExecutionException {
    public TransactionInvalidException(String message) {
        super(message);
    }

    public TransactionInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionInvalidException(Throwable cause) {
        super(cause);
    }

    public TransactionInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
