package com.direwolf20.buildinggadgets.common.util.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public final class UniqueItemStack {
    public final UniqueItem uniqueItem;
    public final CompoundNBT nbt;

    public UniqueItemStack(ItemStack stack) {
        uniqueItem = new UniqueItem(stack.getItem());
        nbt = stack.getTag();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof UniqueItemStack))
            return false;

        UniqueItemStack other = (UniqueItemStack) obj;
        return uniqueItem.equals(other.uniqueItem) && (nbt != null ? nbt.equals(other.nbt) : (other.nbt == null || other.nbt.equals(nbt)));
    }

    @Override
    public int hashCode() {
        return uniqueItem.hashCode() ^ (nbt == null ? 0 : nbt.hashCode());
    }
}