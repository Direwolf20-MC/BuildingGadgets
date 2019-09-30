package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.save.UndoWorldSave.UndoValue;
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

    public void insertUndo(UUID uuid, Undo undo) {
        UndoValue val = getAndUpdateTime(uuid);
        val.getHistory().add(undo);
    }

    public Optional<Undo> getUndo(UUID uuid) {
        return getAndUpdateTime(uuid).getHistory().get();
    }

    public Optional<Undo> peekSnapshot(UUID uuid) {
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

    static final class UndoValue extends TimedDataSave.TimedValue { //for reasons I don't understand it doesn't compile if you leave the TimedDataSave out!
        private final UndoHistory history;

        private UndoValue(CompoundNBT nbt, IntSupplier supplier) {
            super(nbt);
            this.history = new UndoHistory(supplier);
            history.read(nbt);
        }

        private UndoValue(IntSupplier maxLength) {
            super();
            this.history = new UndoHistory(maxLength);
        }

        private UndoHistory getHistory() {
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
