package com.direwolf20.buildinggadgets.common.tools;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static com.direwolf20.buildinggadgets.common.BuildingGadgets.MODID;

public class WorldSave extends WorldSavedData {
    private final String TAG_NAME;

    private Map<String, NBTTagCompound> tagMap = new HashMap<String, NBTTagCompound>();
    //private NBTTagList mapTag;

    public WorldSave(String name, String tagName) {
        super(name);
        TAG_NAME = tagName;
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
        if (nbt.hasKey(TAG_NAME)) {
            NBTTagList tagList = nbt.getTagList(TAG_NAME, Constants.NBT.TAG_COMPOUND);

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
        nbt.setTag(TAG_NAME, tagList);
        return nbt;
    }

    public void markForSaving() {
        markDirty();
    }

    public static WorldSave getWorldSave(World world) {
        return get(world, WorldSaveBlockMap.class);
    }

    public static WorldSave getTemplateWorldSave(World world) {
        return get(world, WorldSaveTemplate.class);
    }

    public static WorldSave getWorldSaveDestruction(World world) {
        return get(world, WorldSaveDestruction.class);
    }

    @Nonnull
    private static WorldSave get(World world, Class<? extends WorldSave> clazz) {
        //boolean isTemplate = clazz == WorldSaveTemplate.class;
        String name = MODID;
        if (clazz == WorldSaveBlockMap.class) {
            name += "_BlockMapData";
        } else if (clazz == WorldSaveTemplate.class) {
            name += "_TemplateData";
        } else if (clazz == WorldSaveDestruction.class) {
            name += "_DestructionUndo";
        }
        //String name = MODID + (isTemplate ? "_TemplateData" : "_BlockMapData");
        MapStorage storage = world.getMapStorage();
        if (storage == null)
            throw new IllegalStateException("World#getMapStorage returned null. The following WorldSave failed to save data: " + name);

        WorldSave instance = (WorldSave) storage.getOrLoadData(clazz, name);

        if (instance == null) {
            if (clazz == WorldSaveBlockMap.class) {
                instance = new WorldSaveBlockMap(name);
            } else if (clazz == WorldSaveTemplate.class) {
                instance = new WorldSaveTemplate(name);
            } else if (clazz == WorldSaveDestruction.class) {
                instance = new WorldSaveDestruction(name);
            }
            //instance = isTemplate ? new WorldSaveTemplate(name) : new WorldSaveBlockMap(name);
            storage.setData(name, instance);
        }
        return instance;
    }

    public static class WorldSaveBlockMap extends WorldSave {
        public WorldSaveBlockMap(String name) {
            super(name, "tagmap");
        }
    }

    public static class WorldSaveTemplate extends WorldSave {
        public WorldSaveTemplate(String name) {
            super(name, "templatedata");
        }
    }

    public static class WorldSaveDestruction extends WorldSave {
        public WorldSaveDestruction(String name) {
            super(name, "destructionundo");
        }
    }
}
