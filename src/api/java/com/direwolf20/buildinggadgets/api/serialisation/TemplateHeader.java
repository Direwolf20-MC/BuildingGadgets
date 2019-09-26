package com.direwolf20.buildinggadgets.api.serialisation;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.util.JsonKeys;
import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import com.google.gson.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

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
     *
     * @param serializer  Template Serializer
     * @param boundingBox the {@link Region} bounding box
     * @return {@link Builder}
     * @see #builder(ResourceLocation, Region)
     */
    public static Builder builder(ITemplateSerializer serializer, Region boundingBox) {
        return builder(Objects.requireNonNull(serializer.getRegistryName()), boundingBox);
    }

    /**
     * Creates a new {@link Builder} which can be used to create {@code TemplateHeader} objects.
     *
     * @param serializer  Information about the {@link ITemplateSerializer}'s id who created the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     * @param boundingBox a {@link BlockPos} representing the x-y-z size of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     * @return A new {@link Builder} for the specified serializer and boundingBox.
     */
    public static Builder builder(SerializerInfo serializer, Region boundingBox) {
        return new Builder(serializer, boundingBox);
    }

    /**
     * Creates a new {@link Builder} which can be used to create {@code TemplateHeader} objects.
     *
     * @param serializer  The {@link ITemplateSerializer}'s id who created the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     * @param boundingBox a {@link BlockPos} representing the x-y-z size of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     * @return A new {@link Builder} for the specified serializer and boundingBox.
     */
    public static Builder builder(ResourceLocation serializer, Region boundingBox) {
        return builder(new SerializerInfo(serializer), boundingBox);
    }

    /**
     * @param header The {@code TemplateHeader} to copy
     * @return a {@link Builder} with all values predefined to the values passed into the header
     */
    public static Builder builderOf(TemplateHeader header) {
        return builderOf(header, header.getSerializerInfo(), header.getBoundingBox());
    }

    /**
     * @param boundingBox the {@link Region} to use
     * @param serializer  The serializer to use
     * @param header      The {@code TemplateHeader} to copy
     * @return a {@link Builder} with all values predefined to the values passed into the header, except for serializer and boundBox
     */
    public static Builder builderOf(TemplateHeader header, SerializerInfo serializer, Region boundingBox) {
        return builder(serializer, boundingBox)
                .author(header.getAuthor())
                .name(header.getName())
                .requiredItems(header.getRequiredItems());
    }

    public static Builder builderFromNBT(CompoundNBT nbt, boolean persisted) {
        Preconditions.checkArgument(nbt.contains(NBTKeys.KEY_SERIALIZER, NBT.TAG_STRING) && nbt.contains(NBTKeys.KEY_BOUNDS, NBT.TAG_COMPOUND),
                "Cannot construct a TemplateHeader without '" + NBTKeys.KEY_SERIALIZER + "' and '" + NBTKeys.KEY_BOUNDS + "'!");
        ResourceLocation serializer = new ResourceLocation(nbt.getString(NBTKeys.KEY_SERIALIZER));
        Region region = Region.deserializeFrom(nbt.getCompound(NBTKeys.KEY_BOUNDS));
        Builder builder = builder(serializer, region);
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

    @Nullable
    private final String name;
    @Nullable
    private final String author;
    @Nullable
    private final MaterialList requiredItems;
    @Nonnull
    private final SerializerInfo serializer;
    @Nonnull
    private final Region boundingBox;

    private TemplateHeader(@Nullable String name, @Nullable String author, @Nullable MaterialList requiredItems, @Nonnull SerializerInfo serializer, @Nonnull Region boundingBox) {
        this.name = name;
        this.author = author;
        this.requiredItems = requiredItems;
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
     * @return The optional set of required Items of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}. Null if not present.
     */
    @Nullable
    public MaterialList getRequiredItems() {
        return requiredItems;
    }

    /**
     * @return The id of the serializer used to create the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     */
    public ResourceLocation getSerializer() {
        return serializer.getSerializer();
    }

    public SerializerInfo getSerializerInfo() {
        return serializer;
    }

    public ResourceLocation getInnerMostSerializer() {
        SerializerInfo info = serializer;
        while (info.getSubSerializer() != null)
            info = info.getSubSerializer();
        return info.getSerializer();
    }

    /**
     * @return The boundingBox of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     */
    public Region getBoundingBox() {
        return boundingBox;
    }

    /**
     * @param persisted whether or not the save may be persisted
     * @return A new {@link CompoundNBT} which can be used for {@link #fromNBT(CompoundNBT)}
     * @implNote If this is called with persisted=false then this will never write {@link #getRequiredItems()}.
     * This is done in order not to prevent updates from changing the required Items for an {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
     */
    public CompoundNBT toNBT(boolean persisted) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString(NBTKeys.KEY_SERIALIZER, getSerializer().toString());
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
        return new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ResourceLocation.class, (JsonSerializer<ResourceLocation>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .registerTypeAdapter(MaterialList.class, new MaterialList.JsonSerializer(printName, extended))
                .registerTypeAdapter(SerializerInfo.class, (JsonSerializer<SerializerInfo>) (src, typeOfSrc, context) -> {
                    if (src.getSubSerializer() != null) {
                        JsonObject obj = new JsonObject();
                        obj.add(JsonKeys.SERIALIZER_INFO_SERIALIZER, context.serialize(src.getSerializer()));
                        obj.add(JsonKeys.SERIALIZER_INFO_SUB_SERIALIZER, context.serialize(src.getSubSerializer()));
                        return obj;
                    } else
                        return context.serialize(src.getSerializer());
                })
                .create()
                .toJson(this);
    }

    /**
     * {@code Builder} for {@link TemplateHeader}. An instance of this class can be acquired via {@link #builder(ResourceLocation, Region)}.
     */
    public static final class Builder {
        @Nullable
        private String name;
        @Nullable
        private String author;
        @Nullable
        private MaterialList requiredItems;
        @Nonnull
        private final SerializerInfo serializer;
        @Nonnull
        private Region boundingBox;

        private Builder(SerializerInfo serializer, Region boundingBox) {
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
         *
         * @param name The name of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
         * @return The {@code Builder} instance to allow for method chaining
         */
        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        /**
         * Set's the author for the resulting {@link TemplateHeader}
         *
         * @param author The author of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
         * @return The {@code Builder} instance to allow for method chaining
         */
        public Builder author(@Nullable String author) {
            this.author = author;
            return this;
        }

        /**
         * Set's the requiredItems for the resulting {@link TemplateHeader}
         *
         * @param requiredItems The requiredItems of the corresponding {@link com.direwolf20.buildinggadgets.api.template.ITemplate}.
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
            return new TemplateHeader(name, author, requiredItems, serializer, boundingBox);
        }
    }

    public static final class SerializerInfo {
        private final ResourceLocation serializer;
        @Nullable
        private final SerializerInfo subSerializer;

        public SerializerInfo(ResourceLocation serializer, @Nullable SerializerInfo subSerializer) {
            this.serializer = Objects.requireNonNull(serializer, "Cannot have SerializerInfo without serializerId");
            this.subSerializer = subSerializer;
        }

        public SerializerInfo(ResourceLocation serializer) {
            this(serializer, null);
        }

        public ResourceLocation getSerializer() {
            return serializer;
        }

        @Nullable
        public SerializerInfo getSubSerializer() {
            return subSerializer;
        }
    }
}
