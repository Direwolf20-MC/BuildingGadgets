package com.direwolf20.buildinggadgets.api.exceptions;


public class TransactionExecutionException extends Exception{
    public TransactionExecutionException(String message) {
        super(message);
    }

    public TransactionExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionExecutionException(Throwable cause) {
        super(cause);
    }

    public TransactionExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
