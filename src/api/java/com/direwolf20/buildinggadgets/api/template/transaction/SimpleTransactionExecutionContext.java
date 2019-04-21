package com.direwolf20.buildinggadgets.api.template.transaction;

import javax.annotation.concurrent.Immutable;

/**
 * Simple implementation of {@link ITransactionExecutionContext} providing a {@link Builder} for creation.
 */
@Immutable
public final class SimpleTransactionExecutionContext implements ITransactionExecutionContext {
    /**
     * @return A new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @param context The context to copy
     * @return A new {@link Builder}, with all values copied from the specified context.
     */
    public static Builder builderOf(ITransactionExecutionContext context) {
        return builder().size(context.getEstimatedTemplateSize());
    }

    private int size;

    private SimpleTransactionExecutionContext(int size) {
        this.size = size;
    }

    /**
     * {@inheritDoc}
     * @return The estimated {@link com.direwolf20.buildinggadgets.api.template.ITemplate}-size. Negative if unknown.
     */
    @Override
    public int getEstimatedTemplateSize() {
        return size;
    }

    /**
     * Builder for creating {@link SimpleTransactionExecutionContext}'s
     */
    public static final class Builder {
        private int size;

        private Builder() {
            this.size = - 1;
        }

        /**
         * Set's the size for the resulting {@link SimpleTransactionExecutionContext}. Defaults to -1 as that indicates unkown.
         * @param size The new size.
         * @return The {@code Builder} instance to allow for Method chaining.
         */
        public Builder size(int size) {
            this.size = size;
            return this;
        }

        /**
         * @return A new {@link SimpleTransactionExecutionContext} with the specified Properties.
         */
        public SimpleTransactionExecutionContext build() {
            return new SimpleTransactionExecutionContext(size);
        }
    }
}
