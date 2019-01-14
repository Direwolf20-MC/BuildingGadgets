package com.direwolf20.buildinggadgets.api;

import net.minecraft.item.Item;

public final class UniqueItem {
    private final Item item;
    private final int meta;

    public UniqueItem(Item i, int m) {
        if (i.getRegistryName() == null) {
            throw new IllegalArgumentException("Attempted to create UniqueItem for an not registered Item! This is not possible!");
        }
        item = i;
        meta = m;
    }

    public int getMeta() {
        return meta;
    }

    public Item getItem() {
        return item;
    }

    public boolean equals(UniqueItem uniqueItem) {
        //item.equals will fall back to reference Equality
        return (uniqueItem.item.equals(item) && uniqueItem.meta == meta);
    }

    @Override
    public int hashCode() {
        assert item.getRegistryName() != null; //An Item without registry name cannot exist in here... Construction would have failed otherwise...
        int result = meta;
        result = 31 * result + item.getRegistryName().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueItem)) return false;

        UniqueItem that = (UniqueItem) o;
        return equals(that);
    }
}
