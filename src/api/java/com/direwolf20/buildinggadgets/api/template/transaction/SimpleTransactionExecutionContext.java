package com.direwolf20.buildinggadgets.api.template.transaction;

public final class SimpleTransactionExecutionContext implements ITransactionExecutionContext {
    public static Builder builder() {
        return new Builder();
    }

    private int size;

    private SimpleTransactionExecutionContext(int size) {
        this.size = size;
    }

    @Override
    public int getEstimatedTemplateSize() {
        return size;
    }

    public static class Builder {
        private int size;

        private Builder() {
            this.size = - 1;
        }

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public SimpleTransactionExecutionContext build() {
            return new SimpleTransactionExecutionContext(size);
        }
    }
}
