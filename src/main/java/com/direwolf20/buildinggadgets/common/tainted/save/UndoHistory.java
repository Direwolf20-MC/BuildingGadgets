package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntSupplier;

final class UndoHistory {
    private final Deque<Undo> history;
    private final IntSupplier maxLengthSupplier;

    UndoHistory(IntSupplier maxLengthSupplier) {
        this.maxLengthSupplier = Objects.requireNonNull(maxLengthSupplier);
        this.history = new LinkedList<>();
    }

    UndoHistory add(Undo undo) {
        history.addFirst(undo);
        ensureSize();
        return this;
    }

    Optional<Undo> get() {
        ensureSize();
        return Optional.ofNullable(history.pollFirst());
    }

    Optional<Undo> peek() {
        ensureSize();
        return Optional.ofNullable(history.peekFirst());
    }

    void read(CompoundNBT nbt) {
        this.history.clear();
        INBT list = nbt.get(NBTKeys.WORLD_SAVE_UNDO_HISTORY);
        if (list instanceof ListNBT) {
            NBTHelper.deserializeCollection((ListNBT) list, history, inbt -> Undo.deserialize((CompoundNBT) inbt));
            ensureSize();
        }
    }

    public void write(CompoundNBT nbt) {
        nbt.put(NBTKeys.WORLD_SAVE_UNDO_HISTORY, NBTHelper.writeIterable(history, Undo::serialize));
    }

    private void ensureSize() {
        int maxLength = maxLengthSupplier.getAsInt();
        Preconditions.checkArgument(maxLength >= 0, "Cannot have a negative max History Length!!!");
        while (history.size() > maxLength) //max-length is non-negative => this always terminates
            history.pollLast();
    }
}
