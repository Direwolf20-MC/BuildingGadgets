package com.direwolf20.buildinggadgets.common.items;

import net.minecraft.item.ItemGroup;

public class BuildingGadget extends Gadget {

    public BuildingGadget() {
        super(ModItems.ITEM_GROUP.maxDamage(0).setNoRepair());
    }
}
