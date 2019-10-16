package com.direwolf20.buildinggadgets.common.tiles;

import com.direwolf20.buildinggadgets.api.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ItemReference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemplateManagerTileEntity extends TileEntity {
    public static final Tag<Item> TEMPLATE_CONVERTIBLES = new ItemTags.Wrapper(ItemReference.TAG_TEMPLATE_CONVERTIBLE);

    public static final int SIZE = 2;

    public TemplateManagerTileEntity() {
        super(OurBlocks.OurTileEntities.TEMPLATE_MANAGER_TYPE);
    }

    // This item handler will hold our inventory slots
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
            TemplateManagerTileEntity.this.markDirty();
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if ((slot == 0 && isNonTemplateStack(stack)))
                return stack;
            if (slot == 1 && isNonTemplateStack(stack) && ! stack.getItem().isIn(TEMPLATE_CONVERTIBLES))
                return stack;
            return super.insertItem(slot, stack, simulate); //handles non-stackable items etc.
        }

        private boolean isNonTemplateStack(ItemStack stack) {
            return ! stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent();
        }
    };
    private LazyOptional<IItemHandler> handlerOpt;

    @Override
    public void onLoad() {
        onChunkUnloaded(); //clear it away if it is still present
        handlerOpt = LazyOptional.of(() -> itemStackHandler);
    }

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
        // If we are too far away (>4 blocks) from this tile entity you cannot use it
        return ! isRemoved() && playerIn.getDistanceSq(new Vec3d(pos).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && handlerOpt != null)
            return handlerOpt.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void onChunkUnloaded() {
        if (handlerOpt != null) {
            handlerOpt.invalidate();
            handlerOpt = null;
        }
    }

    public TemplateManagerContainer getContainer(PlayerEntity playerIn) {
        return new TemplateManagerContainer(0, playerIn.inventory, this);
    }
}
