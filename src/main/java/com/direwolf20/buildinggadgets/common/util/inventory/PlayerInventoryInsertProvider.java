package com.direwolf20.buildinggadgets.common.util.inventory;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class PlayerInventoryInsertProvider implements IInsertProvider {
    private final PlayerEntity player;

    public PlayerInventoryInsertProvider(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public int insert(ItemStack stack, int count, boolean simulate) {
        if (stack.getCount() != count) {
            stack = stack.copy();
            stack.setCount(count);
        }
        ItemEntity itemEntity = new ItemEntity(player.world, player.posX, player.posY, player.posZ, stack);
        itemEntity.onCollideWithPlayer(player);
        return count - itemEntity.getItem().getCount();
    }
}
