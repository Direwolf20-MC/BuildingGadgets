package com.direwolf20.buildinggadgets.api;

import com.google.common.collect.Multiset;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface ITemplate extends INBTSerializable<NBTTagCompound> {
    /**
     * Retrieves the {@link UUID} used by this ITemplate to store Data in the TemplateWorldSave.
     *
     * @return The {@link UUID} for this ITemplate.
     * @implNote Implementations may return null, if this Template has not yet been associated with an WorldSave-ID.
     */
    @Nullable
    public UUID getID();

    /**
     * @return The User specified Name for this Template. Empty if not set
     */
    @Nonnull
    public String getName();

    public BlockPos getStartPos();

    public BlockPos getEndPos();

    /**
     * @return The {@link BlockMap}'s this Template contains
     */
    @Nonnull
    public List<BlockMap> getMappedBlocks();

    public Multiset<UniqueItem> getItemCount();

    /**
     * @param id The Save id to be assigned to this ITemplate
     * @throws NullPointerException  if the provided ID was null.
     * @throws IllegalStateException if this Template already has an assigned ID and this id is not equal to {@link #getID()}
     * @implSpec This Method will do nothing if called with the (Non-null) result from {@link #getID()}.
     */
    public void assignID(@Nonnull UUID id);
}
