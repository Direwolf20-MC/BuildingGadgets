package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.registry.objects.BGContainers;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TemplateManagerContainer extends BaseContainer {
    public static final String TEXTURE_LOC_SLOT_TOOL = Reference.MODID + ":gui/slot_copy_paste_gadget";
    public static final String TEXTURE_LOC_SLOT_TEMPLATE = Reference.MODID + ":gui/slot_template";
    private TemplateManagerTileEntity te;

    public TemplateManagerContainer(int windowId, PlayerInventory playerInventory) {
        super(BGContainers.TEMPLATE_MANAGER_CONTAINER, windowId);//TODO fix once we get access to ContainerTypes
        //addOwnSlots();
        //addPlayerSlots(playerInventory);
    }

    public TemplateManagerContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(BGContainers.TEMPLATE_MANAGER_CONTAINER, windowId);//TODO fix once we get access to ContainerTypes
        BlockPos pos = extraData.readBlockPos();
        this.te = (TemplateManagerTileEntity) Minecraft.getInstance().world.getTileEntity(pos);
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    public TemplateManagerContainer(int windowId, PlayerInventory playerInventory, TemplateManagerTileEntity tileEntity) {
        this(windowId, playerInventory);
        this.te = Objects.requireNonNull(tileEntity);
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return getTe().canInteractWith(playerIn);
    }

    private void addOwnSlots() {
        IItemHandler itemHandler = this.getTe().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(CapabilityNotPresentException::new);
        int x = 86;
        int y = 41;

        addSlot(new SlotTemplateManager(itemHandler, 0, x, y, TEXTURE_LOC_SLOT_TOOL));
        x = 144;
        addSlot(new SlotTemplateManager(itemHandler, 1, x, y, TEXTURE_LOC_SLOT_TEMPLATE));
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(PlayerEntity p_82846_1_, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            //if (!(itemstack1.getItem() instanceof GadgetCopyPaste) && !itemstack1.getItem().equals(Items.PAPER) && !(itemstack1.getItem() instanceof Template))
            //    return itemstack;
            itemstack = itemstack1.copy();

            if (index < TemplateManagerTileEntity.SIZE) {
                if (!this.mergeItemStack(itemstack1, TemplateManagerTileEntity.SIZE, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, TemplateManagerTileEntity.SIZE, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
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
}
