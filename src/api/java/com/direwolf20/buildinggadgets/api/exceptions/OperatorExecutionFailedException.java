package com.direwolf20.buildinggadgets.api.exceptions;

import com.direwolf20.buildinggadgets.api.template.transaction.ITransactionOperator;

public class OperatorExecutionFailedException extends TransactionExecutionException{
    private final ITransactionOperator failingOperator;

    public OperatorExecutionFailedException(String message, ITransactionOperator failingOperator) {
        super(message);
        this.failingOperator = failingOperator;
    }

    public OperatorExecutionFailedException(Throwable cause, ITransactionOperator failingOperator) {
        super(cause);
        this.failingOperator = failingOperator;
    }

    public OperatorExecutionFailedException(String message, Throwable cause, ITransactionOperator failingOperator) {
        super(message, cause);
        this.failingOperator = failingOperator;
    }

    public OperatorExecutionFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ITransactionOperator failingOperator) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.failingOperator = failingOperator;
    }

    public ITransactionOperator getFailingOperator() {
        return failingOperator;
    }
}
