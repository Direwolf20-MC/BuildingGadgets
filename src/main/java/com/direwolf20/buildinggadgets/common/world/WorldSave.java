package com.direwolf20.buildinggadgets.common.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.WorldSavedDataStorage;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static com.direwolf20.buildinggadgets.common.BuildingGadgets.MODID;

public class WorldSave extends WorldSavedData {
    private final String TAG_NAME;

    private Map<String, NBTTagCompound> tagMap = new HashMap<String, NBTTagCompound>();

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
        String name = MODID;

        WorldSave instance = null;
        WorldSavedDataStorage storage = world.getMapStorage();

        if (storage == null)
            throw new IllegalStateException("World#getMapStorage returned null. The following WorldSave failed to save data: " + name);

        if (clazz == WorldSaveBlockMap.class)
            instance = storage.getOrLoadData(WorldSaveBlockMap::new, name);
        else if (clazz == WorldSaveTemplate.class)
            instance = storage.getOrLoadData(WorldSaveTemplate::new, name);
        else if (clazz == WorldSaveDestruction.class)
            instance = storage.getOrLoadData(WorldSaveDestruction::new, name);

        if (instance == null) {
            if (clazz == WorldSaveBlockMap.class) {
                instance = new WorldSaveBlockMap(name);
            } else if (clazz == WorldSaveTemplate.class) {
                instance = new WorldSaveTemplate(name);
            } else if (clazz == WorldSaveDestruction.class) {
                instance = new WorldSaveDestruction(name);
            }

            storage.setData(name, instance);
        }
        return instance;
    }

    @Override
    public void read(NBTTagCompound nbt) {
        if (nbt.hasKey(TAG_NAME)) {
            NBTTagList tagList = nbt.getList(TAG_NAME, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.size(); i++) {
                NBTTagCompound mapTag = tagList.getCompound(i);
                String ID = mapTag.getString("UUID");
                NBTTagCompound tagCompound = mapTag.getCompound("tag");
                tagMap.put(ID, tagCompound);
            }
        }
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        NBTTagList tagList = new NBTTagList();

        for (Map.Entry<String, NBTTagCompound> entry : tagMap.entrySet()) {
            NBTTagCompound map = new NBTTagCompound();
            map.setString("UUID", entry.getKey());
            map.setTag("tag", entry.getValue());
            tagList.add(map);
        }
        compound.setTag(TAG_NAME, tagList);
        return compound;
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
