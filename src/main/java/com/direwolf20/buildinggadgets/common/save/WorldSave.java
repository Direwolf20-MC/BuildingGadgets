package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WorldSave extends WorldSavedData {
    private final String TAG_NAME;

    private Map<String, CompoundNBT> tagMap = new HashMap<String, CompoundNBT>();

    public WorldSave(String name, String tagName) {
        super(name);
        TAG_NAME = tagName;
    }

    public void addToMap(String UUID, CompoundNBT tagCompound) {
        tagMap.put(UUID, tagCompound);
        markDirty();
    }

    public Map<String, CompoundNBT> getTagMap() {
        return tagMap;
    }

    public void setTagMap(Map<String, CompoundNBT> newMap) {
        tagMap = new HashMap<String, CompoundNBT>(newMap);
    }

    public CompoundNBT getCompoundFromUUID(String UUID) {
        CompoundNBT nbt = tagMap.get(UUID);
        return nbt != null ? nbt : new CompoundNBT();
    }

    public void markForSaving() {
        markDirty();
    }

    public static WorldSave getWorldSave(World world) {
        return get(world, WorldSaveBlockMap.NAME, () -> new WorldSaveBlockMap(WorldSaveBlockMap.NAME));
    }

    public static WorldSave getTemplateWorldSave(World world) {
        return get(world, WorldSaveTemplate.NAME, () -> new WorldSaveTemplate(WorldSaveTemplate.NAME));
    }

    @Nonnull
    private static <T extends WorldSave> WorldSave get(World world, String name, Supplier<T> supplier) {
        DimensionSavedDataManager storage = ((ServerWorld) world).getSavedData();
        T data = storage.getOrCreate(supplier, name);
        return data;
    }

    @Override
    public void read(CompoundNBT nbt) {
        if (nbt.contains(TAG_NAME)) {
            ListNBT tagList = nbt.getList(TAG_NAME, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT mapTag = tagList.getCompound(i);
                String ID = mapTag.getString(NBTKeys.WORLD_SAVE_UUID);
                CompoundNBT tagCompound = mapTag.getCompound(NBTKeys.WORLD_SAVE_TAG);
                tagMap.put(ID, tagCompound);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT tagList = new ListNBT();

        for (Map.Entry<String, CompoundNBT> entry : tagMap.entrySet()) {
            CompoundNBT map = new CompoundNBT();
            map.putString(NBTKeys.WORLD_SAVE_UUID, entry.getKey());
            map.put(NBTKeys.WORLD_SAVE_TAG, entry.getValue());
            tagList.add(map);
        }
        compound.put(TAG_NAME, tagList);
        return compound;
    }

    public static class WorldSaveBlockMap extends WorldSave {
        public static final String NAME = "block_map_data";
        public WorldSaveBlockMap(String name) {
            super(name, NAME);
        }
    }

    public static class WorldSaveTemplate extends WorldSave {
        public static final String NAME = "template_data";
        public WorldSaveTemplate(String name) {
            super(name, NAME);
        }
    }
}
