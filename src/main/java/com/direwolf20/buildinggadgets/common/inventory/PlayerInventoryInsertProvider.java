package com.direwolf20.buildinggadgets.common.inventory;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;

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

        int wasPickedUp = ForgeEventFactory.onItemPickup(new ItemEntity(player.world, player.posX, player.posY, player.posZ, copy), player);
        // 0  = no body captured the event and we should handle it by hand.
        if( wasPickedUp == 0 ) {
            player.inventory.addItemStackToInventory(copy);
            if (copy.isEmpty()) {
                return count;
            }
        }

        // If not inserted, allow to fail over
        return 0;
    }
}
