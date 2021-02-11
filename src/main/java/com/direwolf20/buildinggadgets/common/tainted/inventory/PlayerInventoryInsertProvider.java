package com.direwolf20.buildinggadgets.common.tainted.inventory;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * An {@link net.minecraftforge.items.IItemHandler} which inserts old_items into the PlayerInventory by making it pick up a freshly created ItemStack.
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

        int eventResponse = ForgeEventFactory.onItemPickup(new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), copy), player);
        // 0  = picking up was not handled by anyone else, we'll add to inventory what we can.
        if( eventResponse == 0 ) {
            player.inventory.addItemStackToInventory(copy);
            if (copy.isEmpty()) {
                return count;
            } else {
                return count - copy.getCount();
            }
        }
        // eventResponse = -1 or 1, it was handled by someone else, we'll have to assume they handled it all.
        return count;
    }
}
