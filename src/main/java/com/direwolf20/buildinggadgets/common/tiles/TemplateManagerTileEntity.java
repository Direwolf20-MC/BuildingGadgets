package com.direwolf20.buildinggadgets.common.tiles;

import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class TemplateManagerTileEntity extends TileEntity {

    private static final Set<Item> allowedItemsLeft = ImmutableSet.of(OurItems.gadgetCopyPaste, OurItems.template);
    private static final Set<Item> allowedItemsRight = ImmutableSet.of(Items.PAPER, OurItems.template);

    public static final int SIZE = 2;

    public TemplateManagerTileEntity() {
        super(OurBlocks.OurTileEntities.TEMPLATE_MANAGER_TYPE);
    }

    // This item handler will hold our inventory slots
    private ItemStackHandler itemStackHandler = new ItemStackHandler(SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
            TemplateManagerTileEntity.this.markDirty();
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot == 0) {
                if (!(allowedItemsLeft.contains(stack.getItem()))) {
                    return stack;
                }
            } else if (slot == 1) {
                if (!(allowedItemsRight.contains(stack.getItem()))) {
                    return stack;
                }
                if (getStackInSlot(slot).getCount() > 0)
                    return stack;

                if (stack.getCount() > 1) {
                    super.insertItem(slot, ItemHandlerHelper.copyStackWithSize(stack, 1), simulate);
                    return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
                }
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS))
            itemStackHandler.deserializeNBT(compound.getCompound(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS, itemStackHandler.serializeNBT());
        return super.write(compound);
    }

    public boolean canInteractWith(PlayerEntity playerIn) {
        // If we are too far away from this tile entity you cannot use it
        ;
        return ! isRemoved() && playerIn.getDistanceSq(new Vec3d(pos).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> itemStackHandler).cast();
        }

        return super.getCapability(cap, side);
    }

    public TemplateManagerContainer getContainer(PlayerEntity playerIn) {
        return new TemplateManagerContainer(0, playerIn.inventory, this);
    }
}
