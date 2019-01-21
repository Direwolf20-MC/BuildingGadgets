package com.direwolf20.buildinggadgets.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public interface ITemplate {
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

    /**
     * @return The absolute startPos this Template was copied from
     */
    public BlockPos getStartPos();

    /**
     *
     * @return The absolute endPos this Template was copied from
     */
    public BlockPos getEndPos();

    /**
     * @return The {@link BlockMap}'s this Template contains
     */
    @Nonnull
    public ImmutableList<BlockMap> getMappedBlocks();

    /**
     * @param state The state to retrieve a UniqueItem within this Template for
     * @return The {@link UniqueItem} (if known) for the given BlockState
     */
    public UniqueItem getItemFromState(IBlockState state);

    /**
     * @return The amount of times copying was performed on this Templates Data.
     */
    public int getCopyCounter();

    /**
     * @return A Multiset of UniqueItem's
     */
    public ImmutableMultiset<UniqueItem> getItemCount();

    /**
     *
     * @return An Immutable view of this Templates {@link BlockState2ItemMap}
     */
    public DelegatingState2ItemMap getState2ItemMap();
}
