package com.direwolf20.buildinggadgets.common.tileentities;

import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.ItemReference;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TemplateManagerTileEntity extends BlockEntity implements MenuProvider {
    public static final TagKey<Item> TEMPLATE_CONVERTIBLES = TagKey.create(Registry.ITEM_REGISTRY, ItemReference.TAG_TEMPLATE_CONVERTIBLE);

    public static final int SIZE = 2;

    public TemplateManagerTileEntity(BlockPos pos, BlockState state) {
        super(OurTileEntities.TEMPLATE_MANAGER_TILE_ENTITY.get(), pos, state);
    }

    // This item handler will hold our inventory slots
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
            TemplateManagerTileEntity.this.setChanged();
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
                    (slot == 1 && (isTemplateStack(stack) || stack.is(TEMPLATE_CONVERTIBLES)));
        }
    };
    private LazyOptional<IItemHandler> handlerOpt;

    @Override
    @Nonnull
    public Component getDisplayName() {
        return new TextComponent("Template Manager GUI");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity) {
        Preconditions.checkArgument(getLevel() != null);
        return new TemplateManagerContainer(windowId, playerInventory, this);
    }

    @Override
    public void onLoad() {
        onChunkUnloaded(); //clear it away if it is still present
        handlerOpt = LazyOptional.of(() -> itemStackHandler);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS))
            itemStackHandler.deserializeNBT(nbt.getCompound(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS));
    }

    @Nonnull
    @Override
    protected void saveAdditional(CompoundTag compound) {
        compound.put(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS, itemStackHandler.serializeNBT());
        super.saveAdditional(compound);
    }

    public boolean canInteractWith(Player playerIn) {
        // If we are too far away (>4 blocks) from this tile entity you cannot use it
        return !isRemoved() && playerIn.distanceToSqr(Vec3.atLowerCornerOf(worldPosition).add(0.5D, 0.5D, 0.5D)) <= 64D;
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
