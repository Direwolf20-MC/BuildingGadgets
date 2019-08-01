package com.direwolf20.buildinggadgets.api.exceptions;

import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;

public class TransactionResultExceedsTemplateSizeException extends TransactionExecutionException {
    private final ITemplateTransaction transaction;

    public TransactionResultExceedsTemplateSizeException(String message, ITemplateTransaction transaction) {
        super(message);
        this.transaction = transaction;
    }

    public TransactionResultExceedsTemplateSizeException(String message, Throwable cause, ITemplateTransaction transaction) {
        super(message, cause);
        this.transaction = transaction;
    }

    public TransactionResultExceedsTemplateSizeException(Throwable cause, ITemplateTransaction transaction) {
        super(cause);
        this.transaction = transaction;
    }

    public TransactionResultExceedsTemplateSizeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ITemplateTransaction transaction) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.transaction = transaction;
    }

    public ITemplateTransaction getTransaction() {
        return transaction;
    }

    public static class TransactionProvidesToManyDifferentBlockDataInstances extends TransactionResultExceedsTemplateSizeException { //more then 2^32!!!
        public TransactionProvidesToManyDifferentBlockDataInstances(String message, ITemplateTransaction transaction) {
            super(message, transaction);
        }

        public TransactionProvidesToManyDifferentBlockDataInstances(String message, Throwable cause, ITemplateTransaction transaction) {
            super(message, cause, transaction);
        }

        public TransactionProvidesToManyDifferentBlockDataInstances(Throwable cause, ITemplateTransaction transaction) {
            super(cause, transaction);
        }

        public TransactionProvidesToManyDifferentBlockDataInstances(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ITemplateTransaction transaction) {
            super(message, cause, enableSuppression, writableStackTrace, transaction);
        }
    }
}
