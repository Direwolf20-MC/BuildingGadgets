package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.save.TimedDataSave.TimedValue;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.*;
import java.util.function.Function;

public abstract class TimedDataSave<T extends TimedValue> extends WorldSavedData {
    private Map<UUID, T> idToValue;
    private Long2ObjectSortedMap<Set<UUID>> timeToId;

    public TimedDataSave(String name) {
        super(name);
        this.idToValue = new HashMap<>();
        this.timeToId = new Long2ObjectRBTreeMap<>();
    }

    public UUID getFreeUUID() {
        UUID res = UUID.randomUUID();
        return idToValue.containsKey(res) ? getFreeUUID() : res;
    }

    protected T get(UUID id) {
        return get(id, uuid -> createValue());
    }

    protected T get(UUID id, Function<UUID, T> factory) {
        markDirty();
        return idToValue.computeIfAbsent(id, factory);
    }

    protected void remove(UUID id) {
        T val = idToValue.remove(id);
        if (val != null) {
            Set<UUID> set = timeToId.get(val.getUpdateTime());
            if (! set.isEmpty()) {
                set.remove(id);
                if (set.isEmpty())
                    timeToId.remove(val.getUpdateTime());
            }
        }
    }

    protected boolean contains(UUID id) {
        return idToValue.containsKey(id);
    }

    public long getLastUpdateTime(UUID id) {
        return get(id).getUpdateTime();
    }

    @Override
    public void read(CompoundNBT nbt) {
        INBT timeList = nbt.get(NBTKeys.WORD_SAVE_DATA_MAP);
        timeToId.clear();
        idToValue.clear();
        if (timeList instanceof ListNBT) {
            NBTHelper.deserializeUUIDMap((ListNBT) timeList, idToValue, inbt -> readValue((CompoundNBT) inbt));
            for (Map.Entry<UUID, T> entry : idToValue.entrySet()) {
                timeToId.computeIfAbsent(entry.getValue().getUpdateTime(), i -> new HashSet<>()).add(entry.getKey());
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT data = NBTHelper.serializeUUIDMap(idToValue, TimedValue::write);
        compound.put(NBTKeys.WORD_SAVE_DATA_MAP, data);
        return compound;
    }

    protected abstract T createValue();

    protected abstract T readValue(CompoundNBT nbt);

    public static class TimedValue {
        private long lastUpdateTime;

        protected TimedValue(CompoundNBT nbt) {
            this(nbt.contains(NBTKeys.WORLD_SAVE_TIME, NBT.TAG_LONG) ? nbt.getLong(NBTKeys.WORLD_SAVE_TIME) : System.currentTimeMillis());
        }

        protected TimedValue(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        protected TimedValue() {
            this(System.currentTimeMillis());
        }

        public TimedValue updateTime() {
            lastUpdateTime = System.currentTimeMillis();
            return this;
        }

        public long getUpdateTime() {
            return lastUpdateTime;
        }

        public CompoundNBT write() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putLong(NBTKeys.WORLD_SAVE_TIME, lastUpdateTime);
            return nbt;
        }
    }
}
