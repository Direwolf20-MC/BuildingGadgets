package com.direwolf20.buildinggadgets.api.abstraction;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public final class UniqueItem {
    private final Item item;
    @Nullable
    private final CompoundNBT tagCompound;
    private final int hash;

    public UniqueItem(Item item, @Nullable CompoundNBT tagCompound) {
        this.item = Objects.requireNonNull(item);
        this.tagCompound = tagCompound;
        int hash = tagCompound != null ? tagCompound.hashCode() : 0;
        this.hash = Objects.requireNonNull(item.getRegistryName())
                .hashCode() + 31 * hash;
    }

    public Item getItem() {
        return item;
    }

    public ResourceLocation getRegistryName() {
        assert item.getRegistryName() != null; //tested in constructor
        return item.getRegistryName();
    }

    @Nullable
    public CompoundNBT getTag() {
        return tagCompound != null ? tagCompound.copy() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof UniqueItem)) return false;

        UniqueItem that = (UniqueItem) o;

        if (! getRegistryName().equals(that.getRegistryName())) return false;
        return Objects.equals(tagCompound, that.tagCompound);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
