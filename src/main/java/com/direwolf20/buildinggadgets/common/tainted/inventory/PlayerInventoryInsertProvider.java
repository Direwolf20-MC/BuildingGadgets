package com.direwolf20.buildinggadgets.common.tainted.inventory;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * An {@link net.minecraftforge.items.IItemHandler} which inserts items into the PlayerInventory by making it pick up a freshly created ItemStack.
 */
public final class PlayerInventoryInsertProvider implements IInsertProvider {
    private final Player player;

    public PlayerInventoryInsertProvider(Player player) {
        this.player = player;
    }

    @Override
    public int insert(ItemStack stack, int count, boolean simulate) {
        ItemStack copy = stack.copy();
        if (copy.getCount() != count)
            copy.setCount(count);

        int eventResponse = ForgeEventFactory.onItemPickup(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), copy), player);
        // 0  = picking up was not handled by anyone else, we'll add to inventory what we can.
        if( eventResponse == 0 ) {
            player.getInventory().add(copy);
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
