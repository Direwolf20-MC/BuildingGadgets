package com.direwolf20.buildinggadgets.common.blocks.templatemanager;

import com.direwolf20.buildinggadgets.api.*;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.CapabilityProviderTemplate;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.network.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class TemplateManagerContainer extends Container {
    public static final String TEXTURE_LOC_SLOT_TOOL = BuildingGadgets.MODID + ":gui/slot_copypastetool";
    public static final String TEXTURE_LOC_SLOT_TEMPLATE = BuildingGadgets.MODID + ":gui/slot_template";
    private TemplateManagerTileEntity te;

    public TemplateManagerContainer(IInventory playerInventory, TemplateManagerTileEntity te) {
        this.te = te;

        // This container references items out of our own inventory (the 9 slots we hold ourselves)
        // as well as the slots from the player inventory so that the user can transfer items between
        // both inventories. The two calls below make sure that slots are defined for both inventories.
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    public void loadTemplate(EntityPlayer player) {
        ItemStack itemStackDestination = getMutableSlotContent();
        ItemStack itemStackSource = getTemplateSlotContent();
        World world = player.world;
        IMutableTemplate templateDestination = CapabilityProviderTemplate.getPossibleTemplate(itemStackDestination, world);

        ITemplate templateSource = CapabilityProviderTemplate.tryLoadPossibleTemplate(itemStackSource, world);
        if (templateSource == null || templateDestination == null) return;
        templateDestination.copyFrom(templateSource);
        UUID uuid = UUID.randomUUID();
        templateDestination.setId(uuid); //assign a new UUID, as this might have been copied across different saves
        CapabilityProviderTemplate.writeTemplate(templateDestination, itemStackDestination, world);
        putStackInSlot(0, itemStackDestination);
        PacketHandler.INSTANCE.sendTo(new PacketBlockMap(templateDestination.getDataStorage().loadData(uuid, world)), (EntityPlayerMP) player);
    }

    public void saveTemplate(EntityPlayer player, String templateName) {
        ItemStack itemStackSource = getMutableSlotContent();
        ItemStack itemStackDestination = getTemplateSlotContent();
        if (!itemStackDestination.hasCapability(CapabilityBGTemplate.CAPABILITY_MUTABLE_TEMPLATE, null) || itemStackSource.getItem() != Items.PAPER) {
            return;
        }
        //if the only reason, why this is being saved, is in order to set the Name - then don't write World Data
        if (itemStackSource.isEmpty() && !templateName.isEmpty() && itemStackDestination.hasCapability(CapabilityBGTemplate.CAPABILITY_MUTABLE_TEMPLATE, null)) {
            IMutableTemplate template = CapabilityProviderTemplate.getTemplate(itemStackDestination, null);
            template.setName(templateName);
            CapabilityProviderTemplate.writeTemplate(template, itemStackDestination, null);
            putStackInSlot(1, itemStackDestination);
            return;
        }

        if (!(itemStackSource.hasCapability(CapabilityBGTemplate.CAPABILITY_TEMPLATE, null))) {
            return;
        }

        ItemStack templateStack = replacePaperWithTemplate();
        if (templateStack.isEmpty()) return;
        World world = player.world;
        ITemplate source = CapabilityProviderTemplate.tryLoadTemplate(itemStackSource, world); //every ItemStack in the Tile at least has the Template capability
        IMutableTemplate destination = CapabilityProviderTemplate.getTemplate(templateStack, null); //there must be a template to load to, otherwise it would have been empty

        UUID uuid = source.getID();
        if (uuid == null) return;
        destination.copyFrom(source);
        if (!templateName.isEmpty()) destination.setName(templateName);
        writeAndSyncTemplate((EntityPlayerMP) player, world, templateStack, destination);
    }

    public void pasteTemplate(EntityPlayer player, NBTTagCompound sentTagCompound, String templateName) {
        World world = player.world;
        ItemStack templateStack = replacePaperWithTemplate();
        if (templateStack.isEmpty()) return;
        IMutableTemplate template = CapabilityProviderTemplate.getTemplate(templateStack, world); //there must be a template to load to, otherwise it would have been empty
        UUID uuidTemplate = template.getID();
        if (uuidTemplate == null) uuidTemplate = UUID.randomUUID();

        MutableTemplate tempTemplate = new MutableTemplate(WorldSave.TEMPLATE_STORAGE);
        tempTemplate.getMutableState2ItemMap().readNBT(sentTagCompound);
        tempTemplate.getMutableState2ItemMap().initStateItemMap(player); //not the full Data is sent, so this has to be done manually
        tempTemplate.deserializeNBT(sentTagCompound);
        template.onCopy(tempTemplate.getMappedBlocks(), tempTemplate.getStartPos(), tempTemplate.getEndPos(), player);
        template.setId(uuidTemplate); //maybe we are assigning it it's first ID here...
        template.setName(templateName);
        writeAndSyncTemplate((EntityPlayerMP) player, world, templateStack, template);
    }

    private void writeAndSyncTemplate(EntityPlayerMP player, World world, ItemStack templateStack, IMutableTemplate template) {
        CapabilityProviderTemplate.writeTemplate(template, templateStack, world);
        putStackInSlot(1, templateStack);
        if (template.getID() == null) throw new RuntimeException("Expected ID to be set before Sync!");
        PacketHandler.INSTANCE.sendTo(new PacketBlockMap(template.getDataStorage().loadData(template.getID(), world)), player);
    }

    private ItemStack replacePaperWithTemplate() {
        ItemStack templateStack;
        if (getTemplateSlotContent().getItem().equals(Items.PAPER)) {
            templateStack = new ItemStack(ModItems.template, 1);
            putStackInSlot(1, templateStack);
        }
        if (!(getSlot(1).getStack().hasCapability(CapabilityBGTemplate.CAPABILITY_MUTABLE_TEMPLATE, null))) {
            return ItemStack.EMPTY;
        }
        return getSlot(1).getStack();
    }

    private void addPlayerSlots(IInventory playerInventory) {
        // Slots for the main inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + 84;
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 58 + 84;
            this.addSlotToContainer(new Slot(playerInventory, row, x, y));
        }
    }

    private void addOwnSlots() {
        IItemHandler itemHandler = this.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        int x = 86;
        int y = 41;

        // Add our own slots
        //int slotIndex = 0;
        //for (int i = 0; i < itemHandler.getSlots(); i++) {
        addSlotToContainer(new SlotTemplateManager(itemHandler, 0, x, y, TEXTURE_LOC_SLOT_TOOL));
        x = 144;
        addSlotToContainer(new SlotTemplateManager(itemHandler, 1, x, y, TEXTURE_LOC_SLOT_TEMPLATE));
        //slotIndex++;

        //}
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
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

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return te.canInteractWith(playerIn);
    }

    @Nonnull
    public ItemStack getMutableSlotContent() {
        return getSlot(0).getStack();
    }

    public ItemStack getTemplateSlotContent() {
        return getSlot(1).getStack();
    }

    public static class TemplateManagerCommands {

    }
}
