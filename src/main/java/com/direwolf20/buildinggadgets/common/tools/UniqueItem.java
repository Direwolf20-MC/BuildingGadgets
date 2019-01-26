package com.direwolf20.buildinggadgets.common.tools;

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
        return (uniqueItem.item.equals(item) && uniqueItem.meta == meta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueItem)) return false;

        UniqueItem that = (UniqueItem) o;
        return equals(that);
    }

    @Override
    public int hashCode() {
        int result = meta;
        result = 31 * result + item.hashCode();
        return result;
    }
}
