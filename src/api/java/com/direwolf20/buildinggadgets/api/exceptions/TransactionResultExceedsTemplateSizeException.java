package com.direwolf20.buildinggadgets.api.exceptions;

import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import net.minecraft.util.math.BlockPos;

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

    public static class ToManyDifferentBlockDataInstances extends TransactionResultExceedsTemplateSizeException { //more then 2^24!!!
        public ToManyDifferentBlockDataInstances(String message, ITemplateTransaction transaction) {
            super(message, transaction);
        }

        public ToManyDifferentBlockDataInstances(String message, Throwable cause, ITemplateTransaction transaction) {
            super(message, cause, transaction);
        }

        public ToManyDifferentBlockDataInstances(Throwable cause, ITemplateTransaction transaction) {
            super(cause, transaction);
        }

        public ToManyDifferentBlockDataInstances(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ITemplateTransaction transaction) {
            super(message, cause, enableSuppression, writableStackTrace, transaction);
        }
    }

    public static class BlockPosOutOfBounds extends TransactionResultExceedsTemplateSizeException {
        private final BlockPos pos;

        public BlockPosOutOfBounds(String message, ITemplateTransaction transaction, BlockPos pos) {
            super(message, transaction);
            this.pos = pos;
        }

        public BlockPosOutOfBounds(String message, Throwable cause, ITemplateTransaction transaction, BlockPos pos) {
            super(message, cause, transaction);
            this.pos = pos;
        }

        public BlockPosOutOfBounds(Throwable cause, ITemplateTransaction transaction, BlockPos pos) {
            super(cause, transaction);
            this.pos = pos;
        }

        public BlockPosOutOfBounds(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, ITemplateTransaction transaction, BlockPos pos) {
            super(message, cause, enableSuppression, writableStackTrace, transaction);
            this.pos = pos;
        }
    }
}
