package com.direwolf20.buildinggadgets.tools;

import net.minecraft.item.Item;

public class UniqueItem {
    public final int meta;
    public final Item item;

    public UniqueItem(Item i, int m) {
        item = i;
        meta = m;
    }

    public boolean equals(UniqueItem uniqueItem) {
        return (uniqueItem.item == item && uniqueItem.meta == meta);
    }
}
