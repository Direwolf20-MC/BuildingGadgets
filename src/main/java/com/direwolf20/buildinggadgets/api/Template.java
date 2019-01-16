package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.NBTTool;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Template implements ITemplate {
    protected static final String KEY_START_POS = "startPos";
    protected static final String KEY_END_POS = "endPos";
    protected static final String KEY_STATE_MAP = "stateIntArray";
    protected static final String KEY_POS_MAP = "posIntArray";
    protected static final String KEY_COUNT_MAP = "countMap";
    @Nullable
    private UUID id;
    @Nonnull
    private List<BlockMap> blocks;
    @Nonnull
    private Multiset<UniqueItem> itemCountMapping;
    private BlockState2ItemMap state2ItemMap;
    @Nonnull
    private String name;
    private BlockPos endPos;
    private BlockPos startPos;

    public Template() {
        this.id = null;
        this.name = "";
        this.blocks = ImmutableList.of();
        this.itemCountMapping = ImmutableMultiset.of();
        this.endPos = this.startPos = BlockPos.ORIGIN;
        this.state2ItemMap = new BlockState2ItemMap();
    }

    protected void setId(@Nullable UUID id) {
        this.id = id;
    }

    protected BlockState2ItemMap getState2ItemMap() {
        return state2ItemMap;
    }

    protected void setBlocks(@Nonnull List<BlockMap> blocks) {
        this.blocks = blocks;
    }

    protected void setItemCountMapping(@Nonnull Multiset<UniqueItem> itemCountMapping) {
        this.itemCountMapping = itemCountMapping;
    }

    protected void setState2ItemMap(BlockState2ItemMap state2ItemMap) {
        this.state2ItemMap = state2ItemMap;
    }

    protected void setEndPos(BlockPos endPos) {
        this.endPos = endPos;
    }

    protected void setStartPos(BlockPos startPos) {
        this.startPos = startPos;
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
        NBTTagCompound tagCompound = new NBTTagCompound();
        List<BlockMap> blockMaps = getMappedBlocks();
        IntArrayList posMapping = new IntArrayList(blockMaps.size());
        ShortList idMapping = new ShortArrayList(blockMaps.size());
        BlockState2ItemMap state2ItemMap = getState2ItemMap();
        for (BlockMap blockMap:blockMaps) {
            posMapping.add(GadgetUtils.relPosToInt(getStartPos(),blockMap.getPos()));
            idMapping.add(state2ItemMap.getSlot(blockMap.getState()));
        }
        tagCompound.setIntArray(KEY_POS_MAP,posMapping.toIntArray());
        tagCompound.setTag(KEY_STATE_MAP,NBTTool.writeShortList(idMapping));
        tagCompound.setTag(KEY_COUNT_MAP, NBTTool.itemCountToNBT(getItemCount()));
        state2ItemMap.writeToNBT(tagCompound);
        tagCompound.setTag(KEY_START_POS, NBTUtil.createPosTag(getStartPos()));
        tagCompound.setTag(KEY_END_POS, NBTUtil.createPosTag(getEndPos()));
        return tagCompound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        getState2ItemMap().readNBT(nbt);
        if (nbt.hasKey(KEY_START_POS))
            setStartPos(NBTUtil.getPosFromTag((NBTTagCompound) nbt.getTag(KEY_START_POS)));
        if (nbt.hasKey(KEY_END_POS))
            setEndPos(NBTUtil.getPosFromTag((NBTTagCompound) nbt.getTag(KEY_END_POS)));
        if (nbt.hasKey(KEY_COUNT_MAP))
            setItemCountMapping(NBTTool.nbtToItemCount((NBTTagList) nbt.getTag(KEY_COUNT_MAP)));
        IntList posMap;
        ShortList idMap;
        if (nbt.hasKey(KEY_POS_MAP))posMap = new IntArrayList(nbt.getIntArray(KEY_POS_MAP));
        else posMap = new IntArrayList();
        if (nbt.hasKey(KEY_STATE_MAP)) {
            NBTBase tag = nbt.getTag(KEY_STATE_MAP);
            if (tag instanceof NBTTagList) idMap = new ShortArrayList(NBTTool.readShortList((NBTTagList) tag));
            else {
                int[] ar = nbt.getIntArray(KEY_STATE_MAP);
                idMap = new ShortArrayList(ar.length);
                for (int i:ar) {
                    idMap.add((short) i);
                }
            }
        }
        else idMap = new ShortArrayList();
        ImmutableList.Builder<BlockMap> mapping = ImmutableList.builder();
        for (int i = 0; i < posMap.size() && i< idMap.size(); i++) {
            int relPos = posMap.getInt(i);
            BlockPos pos = GadgetUtils.relIntToPos(getStartPos(), relPos);
            short stateId = idMap.getShort(i);
            mapping.add(new BlockMap(pos,getState2ItemMap().getStateFromSlot(stateId),GadgetUtils.relIntToX(relPos), GadgetUtils.relIntToY(relPos), GadgetUtils.relIntToZ(relPos)));
        }
        setBlocks(mapping.build());
    }
}
