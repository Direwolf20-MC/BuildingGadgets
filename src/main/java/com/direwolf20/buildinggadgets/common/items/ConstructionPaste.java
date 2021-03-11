package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ConstructionPaste extends Item {
    public ConstructionPaste() {
        super(OurItems.itemProperties());
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        itemstack = InventoryHelper.addPasteToContainer(player, itemstack);
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }
}
