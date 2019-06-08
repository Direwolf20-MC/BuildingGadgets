package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ConstructionPaste extends Item {
    public ConstructionPaste(Properties builder) {
        super(builder);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, ClientPlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        itemstack = InventoryHelper.addPasteToContainer(player, itemstack);
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

}
