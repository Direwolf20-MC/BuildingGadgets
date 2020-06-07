package com.direwolf20.buildinggadgets.common.inventory;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * An {@link net.minecraftforge.items.IItemHandler} which inserts items into the PlayerInventory by making it pick up a freshly created ItemStack.
 */
public final class PlayerInventoryInsertProvider implements IInsertProvider {
    private final PlayerEntity player;

    public PlayerInventoryInsertProvider(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public int insert(ItemStack stack, int count, boolean simulate) {
        ItemStack copy = stack.copy();
        if (copy.getCount() != count)
            copy.setCount(count);

        ItemEntity itemEntity = new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), copy) {
            //If the stack is completely inserted, then the ItemEntity will reset the count just after it calls remove... We need to catch that 0  count though
            //So we hack our way into remove
            @Override
            public void remove() {
                super.remove();
                stack.setCount(getItem().getCount());
            }
        };

        itemEntity.onCollideWithPlayer(player);
        return Math.min(stack.getCount(), itemEntity.getItem().getCount());
    }
}
