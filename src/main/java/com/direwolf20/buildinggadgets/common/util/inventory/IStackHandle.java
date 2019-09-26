package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import net.minecraft.item.Item;

interface IStackHandle {
    Item getItem();

    int match(UniqueItem item, int count, boolean simulate);

    boolean isReady();
}
