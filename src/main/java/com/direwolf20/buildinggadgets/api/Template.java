package com.direwolf20.buildinggadgets.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Template implements ITemplate {
    @Nonnull
    private List<BlockMap> blocks;
    @Nullable
    private UUID id;
    @Nonnull
    private Multiset<UniqueItem> itemCountMapping;
    @Nonnull
    private String name;
    private BlockPos endPos;
    private BlockPos startPos;

    public Template() {
        this.id = null;
        this.name = "";
        this.blocks = ImmutableList.of();
        this.itemCountMapping = ImmutableMultiset.of();
    }

    protected void setId(@Nullable UUID id) {
        this.id = id;
    }

    /**
     * Retrieves the {@link UUID} used by this ITemplate to store Data in the TemplateWorldSave.
     *
     * @return The {@link UUID} for this ITemplate.
     * @implNote This implementation will return null, if this Template wasn't assigned a UUID yet.
     */
    @Override
    @Nullable
    public UUID getID() {
        return id;
    }

    /**
     * @return The User specified Name for this Template. Empty if not set.
     */
    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    protected void setName(@Nonnull String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public BlockPos getStartPos() {
        return startPos;
    }

    @Override
    public BlockPos getEndPos() {
        return endPos;
    }

    /**
     * @return The {@link BlockMap}'s this Template contains
     */
    @Nonnull
    @Override
    public List<BlockMap> getMappedBlocks() {
        return blocks;
    }

    @Override
    public Multiset<UniqueItem> getItemCount() {
        return itemCountMapping;
    }

    /**
     * @param id The Save id to be assigned to this ITemplate
     * @throws NullPointerException  if the provided ID was null.
     * @throws IllegalStateException if this Template already has an assigned ID and this id is not equal to {@link #getID()}
     * @implSpec This Method will do nothing if called with the (Non-null) result from {@link #getID()}.
     */
    @Override
    public void assignID(@Nonnull UUID id) {
        id = Objects.requireNonNull(id);
        if (getID() == null) {
            setId(id);
        } else if (!getID().equals(id)) {
            throw new IllegalStateException("This Template already has an assigned ID Value and it is impossible to be identified by 2 different ID's!");
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {

    }
}
