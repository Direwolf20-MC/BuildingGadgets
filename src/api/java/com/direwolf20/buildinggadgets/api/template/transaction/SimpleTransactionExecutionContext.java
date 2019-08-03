package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

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
        return builder()
                .size(context.getEstimatedTemplateSize())
                .bounds(context.getBoundingBox())
                .header(context.getHeader());
    }

    private final int size;
    private final Region boundingBox;
    @Nullable
    private final TemplateHeader header;

    private SimpleTransactionExecutionContext(Region boundingBox, @Nullable TemplateHeader header, @Nonnegative int size) {
        this.size = size;
        this.boundingBox = Objects.requireNonNull(boundingBox, "Cannot construct a TransactionExecutionContext with a null BoundingBox!");
        this.header = header;
    }

    /**
     * {@inheritDoc}
     * @return The estimated {@link com.direwolf20.buildinggadgets.api.template.ITemplate Template-size}. Negative if unknown.
     */
    @Override
    public int getEstimatedTemplateSize() {
        return size;
    }

    /**
     * {@inheritDoc}
     *
     * @return A {@link Region} enclosing all positions that may be produced by the {@link com.direwolf20.buildinggadgets.api.template.ITemplate} who created this context
     */
    @Override
    public Region getBoundingBox() {
        return boundingBox;
    }

    @Override
    @Nullable
    public TemplateHeader getHeader() {
        return header;
    }

    /**
     * Builder for creating {@link SimpleTransactionExecutionContext}'s
     */
    public static final class Builder {
        private int size;
        private Region boundingBox;
        private TemplateHeader header;

        private Builder() {
            this.size = - 1;
        }

        /**
         * Set's the size for the resulting {@link SimpleTransactionExecutionContext}. Defaults to -1 as that indicates unkown.
         * @param size The new size.
         * @return The {@code Builder} instance to allow for method chaining.
         */
        public Builder size(int size) {
            this.size = size;
            return this;
        }

        /**
         * Set's the bounding box for the resulting {@link SimpleTransactionExecutionContext}. Defaults to whatever was specified in {@link #build(Region)}.
         * Failing to specify a {@link Region} either here or in {@link #build(Region)} will result in {@link NullPointerException NPE's}.
         *
         * @param boundingBox A {@link Region} enclosing all positions provided by the constructing Template
         * @return The {@code Builder} instance to allow for method chaining.
         */
        public Builder bounds(Region boundingBox) {
            this.boundingBox = Objects.requireNonNull(boundingBox);
            return this;
        }

        /**
         * Set's the {@link TemplateHeader} for the resulting {@link SimpleTransactionExecutionContext}. Defaults to null.
         *
         * @param header The {@link TemplateHeader} to use. May be null.
         * @return The {@code Builder} instance to allow for method chaining.
         */
        public Builder header(@Nullable TemplateHeader header) {
            this.header = header;
            return this;
        }

        /**
         * Same as calling {@link #build(Region) build(null)}
         *
         * @see #build(Region)
         */
        public SimpleTransactionExecutionContext build() {
            return build(null);
        }

        /**
         * @param boundingBox The {@link Region} enclosing all positions. If null the previously specified bounding box will be used.
         * @return A new {@link SimpleTransactionExecutionContext} with the specified Properties.
         */
        public SimpleTransactionExecutionContext build(@Nullable Region boundingBox) {
            if (boundingBox != null)
                this.boundingBox = boundingBox;
            return new SimpleTransactionExecutionContext(this.boundingBox, header, size);
        }


    }
}
