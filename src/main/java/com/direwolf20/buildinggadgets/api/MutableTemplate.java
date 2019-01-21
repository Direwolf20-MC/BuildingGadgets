package com.direwolf20.buildinggadgets.api;

import com.direwolf20.buildinggadgets.common.tools.ArrayUtils;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.NBTTool;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.evaluateDrops;

public class MutableTemplate extends Template implements IMutableTemplate, INBTSerializable<NBTTagCompound> {
    private static final String KEY_ID = "uuid";
    protected static final String KEY_COPY_COUNT = "copycounter";
    protected static final String KEY_NAME = "TemplateName";
    public static final String KEY_START_POS = "startPos";
    public static final String KEY_END_POS = "endPos";
    public static final String KEY_STATE_MAP = "stateIdMap";
    protected static final String KEY_STATE_MAP_OLD = "stateIntArray";
    public static final String KEY_POS_MAP = "posIntArray";
    protected static final String KEY_COUNT_MAP = "countMap";

    private boolean worldDataChanged;
    private boolean loaded;
    private final ITemplateDataStorage dataStorage;

    public MutableTemplate(ITemplateDataStorage storage) {
        super();
        this.worldDataChanged = false;
        this.loaded = false;
        this.dataStorage = storage;
    }

    public boolean hasWorldDataChanged() {
        return worldDataChanged;
    }

    protected void setWorldDataChanged(boolean worldDataChanged) {
        this.worldDataChanged = worldDataChanged;
    }

    protected boolean isLoaded() {
        return loaded;
    }

    private void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public BlockState2ItemMap getMutableState2ItemMap() {
        return super.getMutableState2ItemMap();
    }

    @Override
    public ITemplateDataStorage getDataStorage() {
        return dataStorage;
    }

    @Override
    public void setId(@Nullable UUID id) {
        super.setId(id);
        setWorldDataChanged(true);
    }

    @Override
    protected void setBlocks(@Nonnull List<BlockMap> blocks) {
        super.setBlocks(blocks);
        setWorldDataChanged(true);
    }

    @Override
    protected void setItemCountMapping(@Nonnull Multiset<UniqueItem> itemCountMapping) {
        super.setItemCountMapping(itemCountMapping);
        setWorldDataChanged(true);
    }

    @Override
    protected void setState2ItemMap(BlockState2ItemMap state2ItemMap) {
        super.setState2ItemMap(state2ItemMap);
        setWorldDataChanged(true);
    }

    @Override
    protected void setEndPos(BlockPos endPos) {
        super.setEndPos(endPos);
        setWorldDataChanged(true);
    }

    @Override
    protected void setStartPos(BlockPos startPos) {
        super.setStartPos(startPos);
        setWorldDataChanged(true);
    }

    @Override
    protected void setCopyCounter(int copyCounter) {
        super.setCopyCounter(copyCounter);
        setWorldDataChanged(true);
    }

    @Override
    public void setName(@Nonnull String name) {
        super.setName(name);
        setWorldDataChanged(true);
    }

    private void readStandardNBT(NBTTagCompound tagCompound) {
        if (tagCompound.hasKey(KEY_COPY_COUNT)) {
            setCopyCounter(tagCompound.getInteger(KEY_COPY_COUNT));
        }
        UUID id = NBTTool.readUUID(tagCompound);
        if (id == null) id = UUID.randomUUID();
        setId(id); //this has to be the correct id
    }

    private void writeStandardNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger(KEY_COPY_COUNT, getCopyCounter());
        if (getID() != null) tagCompound.setUniqueId(KEY_ID, getID());
    }

    /**
     * This Method is called to read Data that doesn't need to be stored in the world save, as well as trigger a world data load <b>if necessary</b>.
     *
     * @param nonWorldData The TagCompound storing data outside the WorldSave
     * @param world        The world for which to load the WorldSave for
     */
    @Override
    public void readNBT(@Nonnull NBTTagCompound nonWorldData, @Nullable World world) {
        readStandardNBT(nonWorldData);
        if (getID() != null && world != null) {
            NBTTagCompound nbt = getDataStorage().loadData(getID(), world);
            int counter = getCopyCounter();
            if (nbt != null && (!isLoaded() || (nbt.hasKey(KEY_COPY_COUNT) && nbt.getInteger(KEY_COPY_COUNT) != counter))) {
                deserializeNBT(nbt);
                if (counter != getCopyCounter()) {//overrride the counter - this here should function as the primary source of information
                    setCopyCounter(counter);
                    setWorldDataChanged(true);
                } else {
                    setWorldDataChanged(false);
                }
            }
        }
    }

    /**
     * This Method is called to write Data that doesn't need to be stored in the world save, as well as trigger a world data save <b>if necessary</b>.
     *
     * @param nonWorldData The TagCompound storing data outside the WorldSave
     * @param world        The world for which to save the WorldSave for
     */
    @Override
    public void writeNBT(@Nonnull NBTTagCompound nonWorldData, @Nullable World world) {
        writeStandardNBT(nonWorldData);
        if (hasWorldDataChanged() && getID() != null && world != null) {
            getDataStorage().saveData(getID(), serializeNBT(), world);
            setWorldDataChanged(false);
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        List<BlockMap> blockMaps = getMappedBlocks();
        IntArrayList posMapping = new IntArrayList(blockMaps.size());
        ShortList idMapping = new ShortArrayList(blockMaps.size());
        BlockState2ItemMap state2ItemMap = getMutableState2ItemMap();
        for (BlockMap blockMap : blockMaps) {
            posMapping.add(GadgetUtils.relPosToInt(getStartPos(), blockMap.getPos()));
            idMapping.add(state2ItemMap.getSlot(blockMap.getState()));
        }
        tagCompound.setIntArray(KEY_POS_MAP, posMapping.toIntArray());
        tagCompound.setTag(KEY_STATE_MAP, NBTTool.writeShortList(idMapping));
        tagCompound.setTag(KEY_COUNT_MAP, NBTTool.itemCountToNBT(getItemCount()));
        state2ItemMap.writeToNBT(tagCompound);
        tagCompound.setTag(KEY_START_POS, NBTUtil.createPosTag(getStartPos()));
        tagCompound.setTag(KEY_END_POS, NBTUtil.createPosTag(getEndPos()));
        writeStandardNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        getMutableState2ItemMap().readNBT(nbt);
        if (nbt.hasKey(KEY_START_POS)) {
            setStartPos(NBTUtil.getPosFromTag((NBTTagCompound) nbt.getTag(KEY_START_POS)));
        }
        if (nbt.hasKey(KEY_END_POS)) {
            setEndPos(NBTUtil.getPosFromTag((NBTTagCompound) nbt.getTag(KEY_END_POS)));
        }
        if (nbt.hasKey(KEY_COUNT_MAP)) {
            setItemCountMapping(NBTTool.nbtToItemCount((NBTTagList) nbt.getTag(KEY_COUNT_MAP)));
        }
        if (nbt.hasKey(KEY_NAME)) {
            setName(nbt.getString(KEY_NAME));
        }
        readStandardNBT(nbt);
        IntList posMap;
        ShortList idMap;
        if (nbt.hasKey(KEY_POS_MAP)) {
            posMap = new IntArrayList(nbt.getIntArray(KEY_POS_MAP));
        } else {
            posMap = new IntArrayList();
        }
        if (nbt.hasKey(KEY_STATE_MAP)) {
            idMap = NBTTool.readShortList((NBTTagList) nbt.getTag(KEY_STATE_MAP));
        } else if (nbt.hasKey(KEY_STATE_MAP_OLD)) {
            idMap = ArrayUtils.deepCastToShort(nbt.getIntArray(KEY_STATE_MAP));
        } else {
            idMap = new ShortArrayList();
        }
        buildBlockMappings(posMap, idMap);
    }

    /**
     * Copies all the Data described by the given Template. Will discard any Data currently stored in this instance.
     *
     * @param template The template who is to be cloned
     */
    @Override
    public void copyFrom(ITemplate template) {
        setCopyCounter(template.getCopyCounter() + 1);
        setName(template.getName());
        if (getID() == null) setId(UUID.randomUUID()); //don't copy the other Templates ID - we have to stay unique
        setStartPos(template.getStartPos());
        setEndPos(template.getEndPos());
        setItemCountMapping(template.getItemCount());
        setBlocks(template.getMappedBlocks());
        setState2ItemMap(template.getState2ItemMap().getCopy());
    }

    /**
     * This Method copies the given Mappings into this {@link IMutableTemplate}.
     * Be aware that after this has been called a call to {@link #writeNBT(NBTTagCompound, World)} might be necessary.
     *
     * @param newBlockMap The new List of {@link BlockMap}'s to be used. May be an {@link com.google.common.collect.ImmutableList}.
     * @param start       The origin {@link BlockPos} for this copy
     * @param end         The target {@link BlockPos} for this copy
     * @param thePlayer   The Player who performed the copy and who is to be used for constructing {@link UniqueItem}'s.
     */
    @Override
    public void onCopy(@Nonnull List<BlockMap> newBlockMap, @Nonnull BlockPos start, @Nonnull BlockPos end, @Nonnull EntityPlayer thePlayer) {
        Objects.requireNonNull(newBlockMap);
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        setStartPos(start);
        setEndPos(end);
        setBlocks(newBlockMap);
        setCopyCounter(getCopyCounter() + 1);
        evaluateStateMap(thePlayer);
    }

    protected void evaluateStateMap(@Nonnull EntityPlayer player) {
        ImmutableMultiset.Builder<UniqueItem> itemCountBuilder = ImmutableMultiset.builder();
        getMutableState2ItemMap().clear();
        for (BlockMap blockMap : getMappedBlocks()) {
            UniqueItem item = UniqueItem.fromBlockState(blockMap.getState(), player, blockMap.getPos());
            getMutableState2ItemMap().addToMap(item, blockMap.getState());
            itemCountBuilder.addCopies(item, evaluateDrops(item, blockMap, player));
        }
        setItemCountMapping(itemCountBuilder.build());
    }

    protected void buildBlockMappings(IntList posMap, ShortList idMap) {
        ImmutableList.Builder<BlockMap> mapping = ImmutableList.builder();
        for (int i = 0; i < posMap.size() && i < idMap.size(); i++) {
            int relPos = posMap.getInt(i);
            BlockPos pos = GadgetUtils.relIntToPos(getStartPos(), relPos);
            short stateId = idMap.getShort(i);
            mapping.add(new BlockMap(pos, getMutableState2ItemMap().getStateFromSlot(stateId), GadgetUtils.relIntToX(relPos), GadgetUtils.relIntToY(relPos), GadgetUtils.relIntToZ(relPos)));
        }
        setBlocks(mapping.build());
    }


}
