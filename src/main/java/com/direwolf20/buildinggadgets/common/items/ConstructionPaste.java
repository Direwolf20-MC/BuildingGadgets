package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class ConstructionPaste extends Item {
    public ConstructionPaste() {
        super(OurItems.itemProperties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        itemstack = InventoryHelper.addPasteToContainer(player, itemstack);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }
}
