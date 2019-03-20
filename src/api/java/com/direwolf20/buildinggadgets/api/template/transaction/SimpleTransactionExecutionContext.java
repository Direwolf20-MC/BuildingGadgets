package com.direwolf20.buildinggadgets.api.template.transaction;

/**
 * Simple implementation of {@link ITransactionExecutionContext} providing a {@link Builder} for creation.
 */
public final class SimpleTransactionExecutionContext implements ITransactionExecutionContext {
    /**
     * @return Create a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    private int size;

    private SimpleTransactionExecutionContext(int size) {
        this.size = size;
    }

    /**
     * {@inheritDoc}
     * @return The estimated {@link com.direwolf20.buildinggadgets.api.template.ITemplate}-size. Negative if unkown.
     */
    @Override
    public int getEstimatedTemplateSize() {
        return size;
    }

    public static class Builder {
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
