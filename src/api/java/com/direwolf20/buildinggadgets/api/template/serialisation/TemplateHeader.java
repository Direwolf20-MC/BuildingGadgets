package com.direwolf20.buildinggadgets.api.template.serialisation;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.google.common.collect.Multiset;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class TemplateHeader {
    public static Builder builder(ResourceLocation serializer, BlockPos boundingBox) {
        return new Builder(serializer, boundingBox);
    }

    @Nullable
    private final String name;
    @Nullable
    private final String author;
    @Nullable
    private final Multiset<IUniqueItem> requiredItems;
    @Nonnull
    private final ResourceLocation serializer;
    @Nonnull //Todo replace with Region
    private final BlockPos boundingBox;

    private TemplateHeader(@Nullable String name, @Nullable String author, @Nullable Multiset<IUniqueItem> requiredItems, @Nonnull ResourceLocation serializer, @Nonnull BlockPos boundingBox) {
        this.name = name;
        this.author = author;
        this.requiredItems = requiredItems;
        this.serializer = Objects.requireNonNull(serializer);
        this.boundingBox = Objects.requireNonNull(boundingBox);
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    @Nullable
    public Multiset<IUniqueItem> getRequiredItems() {
        return requiredItems;
    }

    @Nonnull
    public ResourceLocation getSerializer() {
        return serializer;
    }

    @Nonnull
    public BlockPos getBoundingBox() {
        return boundingBox;
    }

    public static final class Builder {
        @Nullable
        private String name;
        @Nullable
        private String author;
        @Nullable
        private Multiset<IUniqueItem> requiredItems;
        @Nonnull
        private final ResourceLocation serializer;
        @Nonnull //Todo replace with Region
        private final BlockPos boundingBox;

        private Builder(@Nonnull ResourceLocation serializer, @Nonnull BlockPos boundingBox) {
            this.serializer = Objects.requireNonNull(serializer);
            this.boundingBox = Objects.requireNonNull(boundingBox);
        }

        public Builder setName(@Nullable String name) {
            this.name = name;
            return this;
        }

        public Builder setAuthor(@Nullable String author) {
            this.author = author;
            return this;
        }

        public Builder setRequiredItems(@Nullable Multiset<IUniqueItem> requiredItems) {
            this.requiredItems = requiredItems;
            return this;
        }

        public TemplateHeader build() {
            return new TemplateHeader(name, author, requiredItems, serializer, boundingBox);
        }
    }
}
