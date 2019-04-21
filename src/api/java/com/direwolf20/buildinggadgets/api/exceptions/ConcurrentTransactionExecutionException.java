package com.direwolf20.buildinggadgets.api.exceptions;

public class ConcurrentTransactionExecutionException extends TransactionExecutionException {
    public ConcurrentTransactionExecutionException(String message) {
        super(message);
    }

    public ConcurrentTransactionExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentTransactionExecutionException(Throwable cause) {
        super(cause);
    }

    public ConcurrentTransactionExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
