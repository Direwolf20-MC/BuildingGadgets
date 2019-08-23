package com.direwolf20.buildinggadgets.api.materials;

import com.direwolf20.buildinggadgets.api.util.NBTKeys;
import com.direwolf20.buildinggadgets.api.util.RegistryUtils;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;

public final class UniqueItem {

    public static UniqueItem deserialize(CompoundNBT res) {
        Preconditions.checkArgument(res.contains(NBTKeys.KEY_ID), "Cannot construct a UniqueItem without an Item!");
        CompoundNBT nbt = res.getCompound(NBTKeys.KEY_DATA);
        Item item;
        if (res.contains(NBTKeys.KEY_ID, NBT.TAG_INT))
            item = RegistryUtils.getById(ForgeRegistries.ITEMS, res.getInt(NBTKeys.KEY_ID));
        else
            item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(res.getString(NBTKeys.KEY_ID)));
        return new UniqueItem(item, nbt.isEmpty() ? null : nbt);
    }

    private final Item item;
    @Nullable
    private final CompoundNBT tagCompound;
    private final int hash;

    public UniqueItem(Item item, @Nullable CompoundNBT tagCompound) {
        this.item = Objects.requireNonNull(item, "Cannot construct a UniqueItem for a null Item!");
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

    public CompoundNBT serialize(boolean persisted) {
        CompoundNBT res = new CompoundNBT();
        if (tagCompound != null)
            res.put(NBTKeys.KEY_DATA, tagCompound);
        if (persisted)
            res.putString(NBTKeys.KEY_ID, item.getRegistryName().toString());
        else
            res.putInt(NBTKeys.KEY_ID, RegistryUtils.getId(ForgeRegistries.ITEMS, item));
        return res;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("item", item)
                .add("tagCompound", tagCompound)
                .toString();
    }
}
