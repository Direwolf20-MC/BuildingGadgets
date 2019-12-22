package com.direwolf20.buildinggadgets.common.gadgets.history;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class HistoryStack {
    private List<HistoryEntry> historyEntries = new ArrayList<>();

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();

        // The items
        historyEntries.forEach(entry -> {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setTag("pos", NBTUtil.createPosTag(entry.getPos()));
            entryTag.setTag("state", NBTUtil.writeBlockState(new NBTTagCompound(), entry.getState()));

            if( entry.getPasteState() != null )
                entryTag.setTag("paste", NBTUtil.writeBlockState(new NBTTagCompound(), entry.getPasteState()));

            list.appendTag(entryTag);
        });

        tag.setTag("entries", list);
        return tag;
    }

    public void deserialize(NBTTagCompound compound) {
        historyEntries.clear();

        NBTTagList list = compound.getTagList("entries", Constants.NBT.TAG_COMPOUND);
        if( list.hasNoTags() )
            return;

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tagCompound = list.getCompoundTagAt(i);

            historyEntries.add(new HistoryEntry(
                    NBTUtil.getPosFromTag(tagCompound.getCompoundTag("pos")),
                    NBTUtil.readBlockState(tagCompound.getCompoundTag("state")),
                    tagCompound.hasKey("paste") ? NBTUtil.readBlockState(tagCompound.getCompoundTag("paste")) : null
            ));
        }
    }

    public List<HistoryEntry> getHistory() {
        return historyEntries;
    }
}
