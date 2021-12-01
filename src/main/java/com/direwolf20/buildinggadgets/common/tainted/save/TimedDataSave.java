package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.tainted.save.TimedDataSave.TimedValue;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.*;
import java.util.function.Function;

public abstract class TimedDataSave<T extends TimedValue> extends SavedData {
    private Map<UUID, T> idToValue;
    private Long2ObjectSortedMap<Set<UUID>> timeToId;

    public TimedDataSave() {
        super();
        this.idToValue = new HashMap<>();
        this.timeToId = new Long2ObjectRBTreeMap<>();
    }

    public UUID getFreeUUID() {
        UUID res = UUID.randomUUID();
        return idToValue.containsKey(res) ? getFreeUUID() : res;
    }

    protected void writeAllIds(FriendlyByteBuf buffer) {
        for (UUID id : idToValue.keySet()) {
            buffer.writeUUID(id);
        }
    }

    protected T get(UUID id) {
        return get(id, uuid -> createValue());
    }

    protected T get(UUID id, Function<UUID, T> factory) {
        setDirty();
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

    public void load(CompoundTag nbt) {
        Tag timeList = nbt.get(NBTKeys.WORD_SAVE_DATA_MAP);
        timeToId.clear();
        idToValue.clear();
        if (timeList instanceof ListTag) {
            NBTHelper.deserializeUUIDMap((ListTag) timeList, idToValue, inbt -> readValue((CompoundTag) inbt));
            for (Map.Entry<UUID, T> entry : idToValue.entrySet()) {
                timeToId.computeIfAbsent(entry.getValue().getUpdateTime(), i -> new HashSet<>()).add(entry.getKey());
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag data = NBTHelper.serializeUUIDMap(idToValue, TimedValue::write);
        compound.put(NBTKeys.WORD_SAVE_DATA_MAP, data);
        return compound;
    }

    protected abstract T createValue();

    protected abstract T readValue(CompoundTag nbt);

    public static class TimedValue {
        private long lastUpdateTime;

        protected TimedValue(CompoundTag nbt) {
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

        public CompoundTag write() {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong(NBTKeys.WORLD_SAVE_TIME, lastUpdateTime);
            return nbt;
        }
    }
}
