package com.direwolf20.buildinggadgets.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Template implements ITemplate {
    //not saved by standard serialisation
    @Nullable
    private UUID id;
    @Nonnull
    private ImmutableList<BlockMap> blocks;
    @Nonnull
    private ImmutableMultiset<UniqueItem> itemCountMapping;
    private BlockState2ItemMap state2ItemMap;
    private DelegatingState2ItemMap state2ItemMapView;
    @Nonnull
    private String name;
    @Nonnull
    private BlockPos endPos;
    @Nonnull
    private BlockPos startPos;
    private int copyCounter;

    public Template() {
        this.id = null;
        this.name = "";
        this.blocks = ImmutableList.of();
        this.itemCountMapping = ImmutableMultiset.of();
        this.endPos = this.startPos = BlockPos.ORIGIN;
        this.state2ItemMap = new BlockState2ItemMap();
        this.state2ItemMapView = new DelegatingState2ItemMap(state2ItemMap);
        this.copyCounter = 0;
    }

    protected void setId(@Nullable UUID id) {
        this.id = id;
    }

    protected BlockState2ItemMap getMutableState2ItemMap() {
        return state2ItemMap;
    }

    protected void setBlocks(@Nonnull List<BlockMap> blocks) {
        this.blocks = ImmutableList.copyOf(blocks);
    }

    protected void setItemCountMapping(@Nonnull Multiset<UniqueItem> itemCountMapping) {
        this.itemCountMapping = ImmutableMultiset.copyOf(itemCountMapping);
    }

    protected void setState2ItemMap(BlockState2ItemMap state2ItemMap) {
        this.state2ItemMap = Objects.requireNonNull(state2ItemMap);
        this.state2ItemMapView = new DelegatingState2ItemMap(state2ItemMap);
    }

    protected void setEndPos(BlockPos endPos) {
        this.endPos = Objects.requireNonNull(endPos);
    }

    protected void setStartPos(BlockPos startPos) {
        this.startPos = Objects.requireNonNull(startPos);
    }

    protected void setCopyCounter(int copyCounter) {
        this.copyCounter = copyCounter;
    }

    protected void setName(@Nonnull String name) {
        this.name = Objects.requireNonNull(name);
    }

    /**
     * @return An Immutable view of this Templates {@link BlockState2ItemMap}
     */
    @Override
    public DelegatingState2ItemMap getState2ItemMap() {
        return state2ItemMapView;
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
    public ImmutableList<BlockMap> getMappedBlocks() {
        return blocks;
    }

    @Override
    public ImmutableMultiset<UniqueItem> getItemCount() {
        return itemCountMapping;
    }

    @Override
    public int getCopyCounter() {
        return copyCounter;
    }

    /**
     * @param state The state to retrieve a UniqueItem within this Template for
     * @return The {@link UniqueItem} (if known) for the given BlockState
     */
    @Override
    public UniqueItem getItemFromState(IBlockState state) {
        return getMutableState2ItemMap().getItemForState(state);
    }
}
