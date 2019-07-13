package com.direwolf20.buildinggadgets.common.containers;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.registry.objects.BGContainers;
import com.direwolf20.buildinggadgets.common.tiles.ChargingStationTileEntity;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ChargingStationContainer extends BaseContainer {
    private ChargingStationTileEntity te;

    public ChargingStationContainer(int windowId, PlayerInventory playerInventory) {
        super(BGContainers.CHARGING_STATION_CONTAINER, windowId);
        //addOwnSlots();
        //addPlayerSlots(playerInventory);
    }

    public ChargingStationContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(BGContainers.CHARGING_STATION_CONTAINER, windowId);
        BlockPos pos = extraData.readBlockPos();
        this.te = (ChargingStationTileEntity) Minecraft.getInstance().world.getTileEntity(pos);
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    public ChargingStationContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player) {
        super(BGContainers.CHARGING_STATION_CONTAINER, windowId);
        this.te = (ChargingStationTileEntity) world.getTileEntity(pos);
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

        addSlot(new SlotItemHandler(itemHandler, 0, x, y));
        x = 144;
        addSlot(new SlotItemHandler(itemHandler, 1, x, y));
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(PlayerEntity p_82846_1_, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();
            if (index < ChargingStationTileEntity.SIZE) {
                if (! this.mergeItemStack(stack, ChargingStationTileEntity.SIZE, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack, itemstack);
            } else {
                int burnTime = net.minecraftforge.event.ForgeEventFactory.getItemBurnTime(stack, stack.getBurnTime() == - 1 ? AbstractFurnaceTileEntity.getBurnTimes().getOrDefault(stack.getItem(), 0) : stack.getBurnTime());
                //System.out.println(burnTime);
                if (stack.getItem() instanceof GadgetGeneric) {
                    if (! this.mergeItemStack(stack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (burnTime > 0) {
                    if (! this.mergeItemStack(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(p_82846_1_, stack);
        }
        detectAndSendChanges();

        return itemstack;
    }

    public ChargingStationTileEntity getTe() {
        return te;
    }

    public int getEnergy() {
        return te.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }
}
