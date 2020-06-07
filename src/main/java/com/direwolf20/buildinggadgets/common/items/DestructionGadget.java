package com.direwolf20.buildinggadgets.common.items;

public class DestructionGadget extends Gadget {

    public DestructionGadget() {
        super(ModItems.ITEM_GROUP.maxDamage(0).setNoRepair());
    }
}
