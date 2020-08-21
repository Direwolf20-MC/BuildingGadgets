package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ItemReference;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemplateManagerTileEntity extends TileEntity implements INamedContainerProvider {
    public static final ITag.INamedTag<Item> TEMPLATE_CONVERTIBLES = ItemTags.makeWrapperTag(ItemReference.TAG_TEMPLATE_CONVERTIBLE.toString());

    public static final int SIZE = 2;

    public TemplateManagerTileEntity() {
        super(OurTileEntities.TEMPLATE_MANAGER_TILE_ENTITY.get());
    }

    // This item handler will hold our inventory slots
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
            TemplateManagerTileEntity.this.markDirty();
        }

        private boolean isTemplateStack(ItemStack stack) {
            return stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return (slot == 0 && isTemplateStack(stack)) ||
                    (slot == 1 && (isTemplateStack(stack) || stack.getItem().isIn(TEMPLATE_CONVERTIBLES)));
        }
    };
    private LazyOptional<IItemHandler> handlerOpt;

    @Override
    @Nonnull
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Template Manager GUI");
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, @Nonnull PlayerInventory playerInventory, @Nonnull PlayerEntity playerEntity) {
        Preconditions.checkArgument(getWorld() != null);
        return new TemplateManagerContainer(windowId, playerInventory, this);
    }

    @Override
    public void onLoad() {
        onChunkUnloaded(); //clear it away if it is still present
        handlerOpt = LazyOptional.of(() -> itemStackHandler);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);

        if (nbt.contains(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS))
            itemStackHandler.deserializeNBT(nbt.getCompound(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS, itemStackHandler.serializeNBT());
        return super.write(compound);
    }

    public boolean canInteractWith(PlayerEntity playerIn) {
        // If we are too far away (>4 blocks) from this tile entity you cannot use it
        return ! isRemoved() && playerIn.getDistanceSq(Vector3d.of(pos).add(0.5D, 0.5D, 0.5D)) <= 64D;
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
}
