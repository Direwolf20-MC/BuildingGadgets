package com.direwolf20.buildinggadgets.common.utils;

import net.minecraft.item.Item;

public final class UniqueItem {
    public final int meta;
    public final Item item;

    public UniqueItem(Item i, int m) {
        item = i;
        meta = m;
    }

    public boolean equals(UniqueItem uniqueItem) {
        //item.equals will fall back to reference Equality
        return uniqueItem.item.equals(item) && uniqueItem.meta == meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return !(o instanceof UniqueItem) ? false : equals((UniqueItem) o);
    }

    @Override
    public int hashCode() {
        return 31 * meta + item.hashCode();
    }
}
