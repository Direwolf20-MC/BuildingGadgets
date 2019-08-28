package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.save.UndoWorldSave.UndoValue;
import com.direwolf20.buildinggadgets.common.util.blocks.RegionSnapshot;
import net.minecraft.nbt.CompoundNBT;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntSupplier;

public class UndoWorldSave extends TimedDataSave<UndoValue> {
    private final IntSupplier undoMaxLength;

    public UndoWorldSave(String name, IntSupplier undoMaxLength) {
        super(name);
        this.undoMaxLength = Objects.requireNonNull(undoMaxLength);
    }

    public void insertSnapshot(UUID uuid, RegionSnapshot snapshot) {
        UndoValue val = getAndUpdateTime(uuid);
        val.getHistory().add(snapshot);
    }

    public Optional<RegionSnapshot> getSnapshot(UUID uuid) {
        return getAndUpdateTime(uuid).getHistory().get();
    }

    public Optional<RegionSnapshot> peekSnapshot(UUID uuid) {
        return getAndUpdateTime(uuid).getHistory().peek();
    }

    public void removeHistory(UUID uuid) {
        remove(uuid);
    }

    private UndoValue getAndUpdateTime(UUID uuid) {
        UndoValue val = get(uuid);
        val.updateTime();
        return val;
    }

    @Override
    protected UndoValue createValue() {
        return new UndoValue(undoMaxLength);
    }

    @Override
    protected UndoValue readValue(CompoundNBT nbt) {
        return new UndoValue(nbt, undoMaxLength);
    }

    static class UndoValue extends TimedValue {
        private final UndoHistory history;

        public UndoValue(CompoundNBT nbt, IntSupplier supplier) {
            super(nbt);
            this.history = new UndoHistory(supplier);
            history.read(nbt);
        }

        public UndoValue(IntSupplier maxLength) {
            this.history = new UndoHistory(maxLength);
        }

        public UndoHistory getHistory() {
            return history;
        }

        @Override
        public CompoundNBT write() {
            CompoundNBT nbt = super.write();
            history.write(nbt);
            return nbt;
        }
    }

}
