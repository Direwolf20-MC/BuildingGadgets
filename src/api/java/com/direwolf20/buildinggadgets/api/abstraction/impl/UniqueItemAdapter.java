package com.direwolf20.buildinggadgets.api.abstraction.impl;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public final class UniqueItemAdapter implements IUniqueItem {
    private final Item item;
    @Nullable
    private final NBTTagCompound tagCompound;
    private final int hash;

    public UniqueItemAdapter(Item item, @Nullable NBTTagCompound tagCompound) {
        this.item = Objects.requireNonNull(item);
        this.tagCompound = tagCompound;
        int hash = tagCompound != null ? tagCompound.hashCode() : 0;
        this.hash = Objects.requireNonNull(item.getRegistryName())
                .hashCode() + 31 * hash;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public ResourceLocation getRegistryName() {
        assert item.getRegistryName() != null; //tested in constructor
        return item.getRegistryName();
    }

    @Nullable
    @Override
    public NBTTagCompound getTag() {
        return tagCompound != null ? tagCompound.copy() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof UniqueItemAdapter)) return false;

        UniqueItemAdapter that = (UniqueItemAdapter) o;

        if (! getRegistryName().equals(that.getRegistryName())) return false;
        return Objects.equals(tagCompound, that.tagCompound);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
