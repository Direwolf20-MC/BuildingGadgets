package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.entities.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TemplateManagerContainer extends BaseContainer {
    public static final String TEXTURE_LOC_SLOT_TOOL = Reference.MODID + ":gui/slot_copy_paste_gadget";
    public static final String TEXTURE_LOC_SLOT_TEMPLATE = Reference.MODID + ":gui/slot_template";

    private TemplateManagerTileEntity te;

    public TemplateManagerContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(OurContainers.TEMPLATE_MANAGER_CONTAINER, windowId);
        BlockPos pos = extraData.readBlockPos();

        this.te = (TemplateManagerTileEntity) playerInventory.player.world.getTileEntity(pos);
        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    public TemplateManagerContainer(int windowId, PlayerInventory playerInventory, TemplateManagerTileEntity tileEntity) {
        super(OurContainers.TEMPLATE_MANAGER_CONTAINER, windowId);
        this.te = Objects.requireNonNull(tileEntity);

        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return getTe().canInteractWith(playerIn);
    }

    private void addOwnSlots() {
        IItemHandler itemHandler = this.getTe().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(CapabilityNotPresentException::new);
        int x = 132;
        addSlot(new SlotTemplateManager(itemHandler, 0, x, 18, TEXTURE_LOC_SLOT_TOOL));
        addSlot(new SlotTemplateManager(itemHandler, 1, x, 63, TEXTURE_LOC_SLOT_TEMPLATE));
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack currentStack = slot.getStack();
            itemstack = currentStack.copy();

            if (index < TemplateManagerTileEntity.SIZE) {
                if (! this.mergeItemStack(currentStack, TemplateManagerTileEntity.SIZE, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (! this.mergeItemStack(currentStack, 0, TemplateManagerTileEntity.SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
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
        public int getSlotStackLimit() {
            return 1;
        }
    }
}
