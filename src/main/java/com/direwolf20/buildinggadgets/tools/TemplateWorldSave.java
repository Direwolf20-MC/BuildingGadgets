package com.direwolf20.buildinggadgets.tools;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

import static com.direwolf20.buildinggadgets.BuildingGadgets.MODID;

public class TemplateWorldSave extends WorldSavedData {
    private static final String DATA_NAME = MODID + "_TemplateData";
    private static final String TAGNAME = "templatedata";

    private Map<String, NBTTagCompound> tagMap = new HashMap<String, NBTTagCompound>();
    //private NBTTagList mapTag;

    public TemplateWorldSave() {
        super(DATA_NAME);
    }

    public TemplateWorldSave(String s) {
        super(s);
    }

    public void addToMap(String UUID, NBTTagCompound tagCompound) {
        tagMap.put(UUID, tagCompound);
        markDirty();
    }

    public Map<String, NBTTagCompound> getTagMap() {
        return tagMap;
    }

    public void setTagMap(Map<String, NBTTagCompound> newMap) {
        tagMap = new HashMap<String, NBTTagCompound>(newMap);
    }

    public NBTTagCompound getCompoundFromUUID(String UUID) {
        return tagMap.get(UUID);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(TAGNAME)) {
            NBTTagList tagList = nbt.getTagList(TAGNAME, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound mapTag = tagList.getCompoundTagAt(i);
                String ID = mapTag.getString("UUID");
                NBTTagCompound tagCompound = mapTag.getCompoundTag("tag");
                tagMap.put(ID, tagCompound);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList tagList = new NBTTagList();

        for (Map.Entry<String, NBTTagCompound> entry : tagMap.entrySet()) {
            NBTTagCompound map = new NBTTagCompound();
            map.setString("UUID", entry.getKey());
            map.setTag("tag", entry.getValue());
            tagList.appendTag(map);
        }
        nbt.setTag(TAGNAME, tagList);
        return nbt;
    }

    public void markForSaving() {
        markDirty();
    }

    public static TemplateWorldSave get(World world) {
        MapStorage storage = world.getMapStorage();
        TemplateWorldSave instance = (TemplateWorldSave) storage.getOrLoadData(TemplateWorldSave.class, DATA_NAME);

        if (instance == null) {
            instance = new TemplateWorldSave();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }
}
