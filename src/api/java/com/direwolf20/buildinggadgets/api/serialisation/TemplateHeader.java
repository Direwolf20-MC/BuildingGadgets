package com.direwolf20.buildinggadgets.api.serialisation;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.google.common.collect.Multiset;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * In-Memory representation of the '.th' (TemplateHeader) written in json Format to disk, in order to allow users to have some more general Information about the
 * {@link com.direwolf20.buildinggadgets.api.template.ITemplate}. Only the registryName of the {@link ITemplateSerializer} used to create the
 * {@link com.direwolf20.buildinggadgets.api.template.ITemplate} and some boundingBox information is required. However it is advised to provide Users with as
 * much information as possible about the {@link com.direwolf20.buildinggadgets.api.template.ITemplate} they would like to use. This class is likely to be
 * expanded over time, as we get informed about other useful things to be put in here.
 */
public final class TemplateHeader {
    /**
     * Convenience overload taking an {@link ITemplateSerializer} instead of it's registryName-
     * @see #builder(ResourceLocation, Region)
     */
    public static Builder builder(ITemplateSerializer serializer, Region boundingBox) {
        return builder(Objects.requireNonNull(serializer.getRegistryName()), boundingBox);
    }

    /**
     * Creates a new {@link Builder} which can be used to create {@code TemplateHeader} objects.
     * @param serializer The {@link ITemplateSerializer}'s id who created the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     * @param boundingBox a {@link BlockPos} representing the x-y-z size of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     * @return A new {@link Builder} for the specified serializer and boundingBox.
     */
    public static Builder builder(ResourceLocation serializer, Region boundingBox) {
        return new Builder(serializer, boundingBox);
    }

    /**
     * @param header The {@code TemplateHeader} to copy
     * @return a Builder with all values predefined to the values passed into the header
     */
    public static Builder builderOf(TemplateHeader header) {
        return builder(header.getSerializer(), header.getBoundingBox())
                .author(header.getAuthor())
                .name(header.getName())
                .requiredItems(header.getRequiredItems());
    }

    @Nullable
    private final String name;
    @Nullable
    private final String author;
    @Nonnull
    private final MaterialList requiredItems;
    @Nonnull
    private final ResourceLocation serializer;
    @Nonnull
    private final Region boundingBox;

    private TemplateHeader(@Nullable String name, @Nullable String author, @Nullable MaterialList requiredItems, @Nonnull ResourceLocation serializer, @Nonnull Region boundingBox) {
        this.name = name;
        this.author = author;
        this.requiredItems = requiredItems != null ? requiredItems : MaterialList.empty();
        this.serializer = Objects.requireNonNull(serializer);
        this.boundingBox = Objects.requireNonNull(boundingBox);
    }

    /**
     * @return The optional name of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}. Null if not present.
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * @return The optional author of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}. Null if not present.
     */
    @Nullable
    public String getAuthor() {
        return author;
    }

    /**
     * @return The optional set of required Items of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}. Empty if not present.
     */
    @Nonnull
    public MaterialList getRequiredItems() {
        return requiredItems;
    }

    /**
     * @return The id of the serializer used to create the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     */
    @Nonnull
    public ResourceLocation getSerializer() {
        return serializer;
    }

    /**
     * @return The boundingBox of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     */
    @Nonnull
    public Region getBoundingBox() {
        return boundingBox;
    }

    /**
     * Builder for {@link TemplateHeader}. An instance of this class can be acquired via {@link #builder(ResourceLocation, Region)}.
     */
    public static final class Builder {
        @Nullable
        private String name;
        @Nullable
        private String author;
        @Nullable
        private MaterialList requiredItems;
        @Nonnull
        private final ResourceLocation serializer;
        @Nonnull
        private Region boundingBox;

        private Builder(ResourceLocation serializer, Region boundingBox) {
            this.serializer = Objects.requireNonNull(serializer);
            this.boundingBox = Objects.requireNonNull(boundingBox);
        }

        /**
         * @param boundingBox The new boundingBox to be used. May not be null!
         * @return The {@code Builder} instance to allow for method chaining
         */
        public Builder bounds(Region boundingBox) {
            this.boundingBox = Objects.requireNonNull(boundingBox);
            return this;
        }

        /**
         * Set's the name for the resulting {@link TemplateHeader}
         * @param name The name of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
         * @return The {@code Builder} instance to allow for method chaining
         */
        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        /**
         * Set's the author for the resulting {@link TemplateHeader}
         * @param author The author of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
         * @return The {@code Builder} instance to allow for method chaining
         */
        public Builder author(@Nullable String author) {
            this.author = author;
            return this;
        }

        /**
         * Set's the requiredItems for the resulting {@link TemplateHeader}
         * @param requiredItems The requiredItems of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
         *         Null values will be converted to an empty {@link Multiset}.
         * @return The {@code Builder} instance to allow for method chaining
         */
        public Builder requiredItems(@Nullable MaterialList requiredItems) {
            this.requiredItems = requiredItems;
            return this;
        }

        /**
         * @return A new {@link TemplateHeader} with the specified properties.
         */
        public TemplateHeader build() {
            return new TemplateHeader(name, author, requiredItems, serializer, boundingBox);
        }
    }
}
