package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.api.UniqueItem;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Utility class providing various Methods for handling NBTData
 */
public class NBTTool {
    private static final String KEY_ID_OLD = "UUID";
    private static final String KEY_ID = "uuid";

    @Nullable
    public static UUID readUUID(NBTTagCompound tagCompound) {
        UUID id = null;
        if (tagCompound.hasKey(KEY_ID)) {
            id = tagCompound.getUniqueId(KEY_ID);
        } else if (tagCompound.hasKey(KEY_ID_OLD)) {
            String rep = tagCompound.getString(KEY_ID);
            if (!rep.isEmpty()) id = UUID.fromString(rep);
        }
        return id;
    }

    public static void writeUUID(UUID id, NBTTagCompound tagCompound) {
        tagCompound.setUniqueId(KEY_ID, id);
    }

    public static NBTTagList itemCountToNBT(Multiset<UniqueItem> itemCountMap) {
        NBTTagList tagList = new NBTTagList();

        for (Multiset.Entry<UniqueItem> entry : itemCountMap.entrySet()) {
            int count = entry.getCount();
            NBTTagCompound tagCompound = new NBTTagCompound();
            entry.getElement().writeToNBT(tagCompound);
            tagCompound.setInteger("count", count);
            tagList.appendTag(tagCompound);
        }
        return tagList;
    }

    public static Multiset<UniqueItem> nbtToItemCount(@Nullable NBTTagList tagList) {
        if (tagList == null) return ImmutableMultiset.of();
        ImmutableMultiset.Builder<UniqueItem> itemCountMap = ImmutableMultiset.builder();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
            UniqueItem uniqueItem = UniqueItem.readFromNBT(tagCompound);
            int count = tagCompound.getInteger("count");
            itemCountMap.setCount(uniqueItem, count);
        }

        return itemCountMap.build();
    }

    public static NBTTagList writeShortList(ShortList shorts) {
        NBTTagList list = new NBTTagList();
        for (short s:shorts) {
            list.appendTag(new NBTTagShort(s));
        }
        return list;
    }

    public static ShortList readShortList(NBTTagList tags) {
        ShortList list = new ShortArrayList(tags.tagCount());
        for (NBTBase nbt:tags) {
            if (nbt instanceof NBTTagShort)
                list.add(((NBTTagShort) nbt).getShort());
        }
        return list;
    }
}