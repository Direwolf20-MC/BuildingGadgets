package com.direwolf20.buildinggadgets.common.tools;

import net.minecraft.item.Item;

public final class UniqueItem {
    public final Item item;

    public UniqueItem(Item i) {
        item = i;
    }

    public boolean equals(UniqueItem uniqueItem) {
        //item.equals will fall back to reference Equality
        return uniqueItem.item.equals(item);
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
        return 31 * item.hashCode();
    }
}
