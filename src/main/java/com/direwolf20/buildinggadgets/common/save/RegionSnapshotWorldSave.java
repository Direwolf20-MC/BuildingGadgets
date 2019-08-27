package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.util.blocks.RegionSnapshot;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class RegionSnapshotWorldSave extends WorldSavedData {
    private final Map<UUID, RegionSnapshot> idToSnapshot;

    public RegionSnapshotWorldSave(String name) {
        super(name);
        this.idToSnapshot = new HashMap<>();
    }

    public UUID getFreeUUID() {
        UUID res = UUID.randomUUID();
        return idToSnapshot.containsKey(res) ? getFreeUUID() : res;
    }

    public void insertSnapshot(UUID uuid, RegionSnapshot snapshot) {
        idToSnapshot.put(uuid, snapshot);
    }

    public Optional<RegionSnapshot> getSnapshot(UUID uuid) {
        return Optional.ofNullable(idToSnapshot.get(uuid));
    }

    public void removeSnapshot(UUID uuid) {
        idToSnapshot.remove(uuid);
    }

    @Override
    public void read(CompoundNBT nbt) {
        idToSnapshot.clear();
        NBTHelper.deserializeUUIDMap((ListNBT) nbt.get(NBTKeys.WORLD_SAVE_TAG), idToSnapshot, c -> RegionSnapshot.deserialize((CompoundNBT) c));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(NBTKeys.WORLD_SAVE_TAG, NBTHelper.serializeUUIDMap(idToSnapshot, RegionSnapshot::serialize));
        return compound;
    }
}
