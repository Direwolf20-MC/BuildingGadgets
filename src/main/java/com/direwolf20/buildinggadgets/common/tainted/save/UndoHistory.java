package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;

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

    void read(CompoundTag nbt) {
        this.history.clear();
        Tag list = nbt.get(NBTKeys.WORLD_SAVE_UNDO_HISTORY);
        if (list instanceof ListTag) {
            NBTHelper.deserializeCollection((ListTag) list, history, inbt -> Undo.deserialize((CompoundTag) inbt));
            ensureSize();
        }
    }

    public void write(CompoundTag nbt) {
        nbt.put(NBTKeys.WORLD_SAVE_UNDO_HISTORY, NBTHelper.writeIterable(history, Undo::serialize));
    }

    private void ensureSize() {
        int maxLength = maxLengthSupplier.getAsInt();
        Preconditions.checkArgument(maxLength >= 0, "Cannot have a negative max History Length!!!");
        while (history.size() > maxLength) //max-length is non-negative => this always terminates
            history.pollLast();
    }
}
