package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.tileentities.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TemplateManagerContainer extends BaseContainer {
    public static final String TEXTURE_LOC_SLOT_TOOL = Reference.MODID + ":gui/slot_copy_paste_gadget";
    public static final String TEXTURE_LOC_SLOT_TEMPLATE = Reference.MODID + ":gui/slot_template";

    private TemplateManagerTileEntity te;

    public TemplateManagerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(OurContainers.TEMPLATE_MANAGER_CONTAINER.get(), windowId);
        BlockPos pos = extraData.readBlockPos();

        this.te = (TemplateManagerTileEntity) playerInventory.player.level.getBlockEntity(pos);
        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    public TemplateManagerContainer(int windowId, Inventory playerInventory, TemplateManagerTileEntity tileEntity) {
        super(OurContainers.TEMPLATE_MANAGER_CONTAINER.get(), windowId);
        this.te = Objects.requireNonNull(tileEntity);

        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return getTe().canInteractWith(playerIn);
    }

    private void addOwnSlots() {
        IItemHandler itemHandler = this.getTe().getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(CapabilityNotPresentException::new);
        int x = 132;
        addSlot(new SlotTemplateManager(itemHandler, 0, x, 18, TEXTURE_LOC_SLOT_TOOL));
        addSlot(new SlotTemplateManager(itemHandler, 1, x, 63, TEXTURE_LOC_SLOT_TEMPLATE));
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem();
            itemstack = currentStack.copy();

            if (index < TemplateManagerTileEntity.SIZE) {
                if (! this.moveItemStackTo(currentStack, TemplateManagerTileEntity.SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (! this.moveItemStackTo(currentStack, 0, TemplateManagerTileEntity.SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public TemplateManagerTileEntity getTe() {
        return te;
    }

    public static class SlotTemplateManager extends SlotItemHandler {
        private String backgroundLoc;

        public SlotTemplateManager(IItemHandler itemHandler, int index, int xPosition, int yPosition, String backgroundLoc) {
            super(itemHandler, index, xPosition, yPosition);
            this.backgroundLoc = backgroundLoc;
        }

        @Override
        public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
            return super.setBackground(atlas, new ResourceLocation(Reference.MODID, this.backgroundLoc));
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
