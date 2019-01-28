package com.direwolf20.buildinggadgets.common.blocks.templatemanager;

import com.direwolf20.buildinggadgets.common.BuildingObjects;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.OptionalCapabilityInstance;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Set;

public class TemplateManagerTileEntity extends TileEntity {

    private static final Set<Item> allowedItemsLeft = ImmutableSet.of(BuildingObjects.gadgetCopyPaste, BuildingObjects.template);
    private static final Set<Item> allowedItemsRight = ImmutableSet.of(Items.PAPER, BuildingObjects.template);

    public static final int SIZE = 2;

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

    public TemplateManagerTileEntity() {
        super(BuildingObjects.TEMPLATE_MANAGER_TYPE);
    }

    @Override
    public void read(NBTTagCompound compound) {
        super.read(compound);

        if (compound.hasKey("items")) {
            itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
    }

    @Override
    public NBTTagCompound write(NBTTagCompound compound) {
        compound.setTag("items", itemStackHandler.serializeNBT());
        return super.write(compound);
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return !isRemoved() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Nonnull
    @Override
    public <T> OptionalCapabilityInstance<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return OptionalCapabilityInstance.of(() -> itemStackHandler).cast();
        }

        return OptionalCapabilityInstance.empty();
    }

    public TemplateManagerContainer getContainer(EntityPlayer playerIn) {
        return new TemplateManagerContainer(playerIn.inventory, this);
    }
}
