package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.util.blocks.RegionSnapshot;
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

public final class UndoHistory {
    private final Deque<RegionSnapshot> history;
    private final IntSupplier maxLengthSupplier;

    public UndoHistory(IntSupplier maxLengthSupplier) {
        this.maxLengthSupplier = Objects.requireNonNull(maxLengthSupplier);
        this.history = new LinkedList<>();
    }

    public UndoHistory add(RegionSnapshot snapshot) {
        history.addFirst(snapshot);
        ensureSize();
        return this;
    }

    public Optional<RegionSnapshot> get() {
        ensureSize();
        return Optional.ofNullable(history.pollFirst());
    }

    public Optional<RegionSnapshot> peek() {
        ensureSize();
        return Optional.ofNullable(history.peekFirst());
    }

    public void read(CompoundNBT nbt) {
        this.history.clear();
        INBT list = nbt.get(NBTKeys.WORLD_SAVE_UNDO);
        if (list instanceof ListNBT) {
            NBTHelper.deserializeCollection((ListNBT) list, history, inbt -> RegionSnapshot.deserialize((CompoundNBT) inbt));
            ensureSize();
        }
    }

    public void write(CompoundNBT nbt) {
        nbt.put(NBTKeys.WORLD_SAVE_UNDO, NBTHelper.writeIterable(history, RegionSnapshot::serialize));
    }

    private void ensureSize() {
        int maxLength = maxLengthSupplier.getAsInt();
        Preconditions.checkArgument(maxLength >= 0, "Cannot have a negative max History Length!!!");
        while (history.size() > maxLength) //max-length is non-negative => this always terminates
            history.pollLast();
    }
}
