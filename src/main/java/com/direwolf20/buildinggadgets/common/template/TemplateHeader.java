package com.direwolf20.buildinggadgets.common.template;

import com.direwolf20.buildinggadgets.common.building.Region;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * In-Memory representation of the '.th' (TemplateHeader) written in json Format to disk, in order to allow users to have some more general Information about the
 * {@link Template}. Only the  boundingBox information is required. However it is advised to provide Users with as
 * much information as possible about the {@link Template} they would like to use.
 */
public final class TemplateHeader {

    /**
     * Creates a new {@link Builder} which can be used to create {@code TemplateHeader} objects.
     *
     * @param boundingBox a {@link BlockPos} representing the x-y-z size of the corresponding {@link Template}.
     * @return A new {@link Builder} for the specified serializer and boundingBox.
     */
    public static Builder builder(Region boundingBox) {
        return new Builder(boundingBox);
    }

    /**
     * @param header The {@code TemplateHeader} to copy
     * @return a {@link Builder} with all values predefined to the values passed into the header
     */
    public static Builder builderOf(TemplateHeader header) {
        return builderOf(header, header.getBoundingBox());
    }

    /**
     * @param boundingBox the {@link Region} to use
     * @param header      The {@code TemplateHeader} to copy
     * @return a {@link Builder} with all values predefined to the values passed into the header, except for serializer and boundBox
     */
    public static Builder builderOf(TemplateHeader header, Region boundingBox) {
        return builder(boundingBox)
                .author(header.getAuthor())
                .name(header.getName())
                .requiredItems(header.getRequiredItems());
    }

    public static Builder builderFromNBT(CompoundNBT nbt, boolean persisted) {
        Preconditions.checkArgument(nbt.contains(NBTKeys.KEY_BOUNDS, NBT.TAG_COMPOUND),
                "Cannot construct a TemplateHeader without '" + NBTKeys.KEY_BOUNDS + "'!");
        Region region = Region.deserializeFrom(nbt.getCompound(NBTKeys.KEY_BOUNDS));
        Builder builder = builder(region);
        if (nbt.contains(NBTKeys.KEY_NAME, NBT.TAG_STRING))
            builder.name(nbt.getString(NBTKeys.KEY_NAME));
        if (nbt.contains(NBTKeys.KEY_AUTHOR, NBT.TAG_STRING))
            builder.name(nbt.getString(NBTKeys.KEY_AUTHOR));
        if (nbt.contains(NBTKeys.KEY_MATERIALS, NBT.TAG_COMPOUND))
            builder.requiredItems(MaterialList.deserialize(nbt.getCompound(NBTKeys.KEY_MATERIALS), persisted));
        return builder;
    }

    public static Builder builderFromNBT(CompoundNBT nbt) {
        return builderFromNBT(nbt, true);
    }

    public static TemplateHeader fromNBT(CompoundNBT nbt) {
        return TemplateHeader.builderFromNBT(nbt).build();
    }

    public static GsonBuilder appendHeaderSpecification(GsonBuilder builder, boolean printName, boolean extended) {
        return builder
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ResourceLocation.class, (JsonSerializer<ResourceLocation>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(MaterialList.class, new MaterialList.JsonSerializer(printName, extended));
    }

    @Nullable
    private final String name;
    @Nullable
    private final String author;
    @Nonnull
    private final Region boundingBox;
    @Nullable
    private final MaterialList requiredItems;

    private TemplateHeader(@Nullable String name, @Nullable String author, @Nullable MaterialList requiredItems, @Nonnull Region boundingBox) {
        this.name = name;
        this.author = author;
        this.requiredItems = requiredItems;
        this.boundingBox = Objects.requireNonNull(boundingBox);
    }

    /**
     * @return The optional name of the corresponding {@link Template}. Null if not present.
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * @return The optional author of the corresponding {@link Template}. Null if not present.
     */
    @Nullable
    public String getAuthor() {
        return author;
    }

    /**
     * @return The optional set of required Items of the corresponding {@link Template}. Null if not present.
     */
    @Nullable
    public MaterialList getRequiredItems() {
        return requiredItems;
    }

    /**
     * @return The boundingBox of the corresponding {@link Template}.
     */
    public Region getBoundingBox() {
        return boundingBox;
    }

    /**
     * @param persisted whether or not the save may be persisted
     * @return A new {@link CompoundNBT} which can be used for {@link #fromNBT(CompoundNBT)}
     * @implNote If this is called with persisted=false then this will never write {@link #getRequiredItems()}.
     * This is done in order not to prevent updates from changing the required Items for an {@link Template}.
     */
    public CompoundNBT toNBT(boolean persisted) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(NBTKeys.KEY_BOUNDS, getBoundingBox().serialize());
        if (getName() != null)
            nbt.putString(NBTKeys.KEY_NAME, getName());
        if (getAuthor() != null)
            nbt.putString(NBTKeys.KEY_AUTHOR, getAuthor());
        if (! persisted && getRequiredItems() != null)
            nbt.put(NBTKeys.KEY_MATERIALS, getRequiredItems().serialize(persisted));
        return nbt;
    }


    public String toJson(boolean printName, boolean extended) {
        return appendHeaderSpecification(new GsonBuilder(), printName, extended)
                .create()
                .toJson(this);
    }

    /**
     * {@code Builder} for {@link TemplateHeader}. An instance of this class can be acquired via {@link #builder(Region)}.
     */
    public static final class Builder {
        @Nullable
        private String name;
        @Nullable
        private String author;
        @Nullable
        private MaterialList requiredItems;
        @Nonnull
        private Region boundingBox;

        private Builder(Region boundingBox) {
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
         *
         * @param name The name of the corresponding {@link Template}.
         * @return The {@code Builder} instance to allow for method chaining
         */
        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        /**
         * Set's the author for the resulting {@link TemplateHeader}
         *
         * @param author The author of the corresponding {@link Template}.
         * @return The {@code Builder} instance to allow for method chaining
         */
        public Builder author(@Nullable String author) {
            this.author = author;
            return this;
        }

        /**
         * Set's the requiredItems for the resulting {@link TemplateHeader}
         *
         * @param requiredItems The requiredItems of the corresponding {@link Template}.
         *                      Null values will be converted to an empty {@link Multiset}.
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
            return new TemplateHeader(name, author, requiredItems, boundingBox);
        }
    }
}
