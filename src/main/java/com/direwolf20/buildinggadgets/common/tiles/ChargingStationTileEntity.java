package com.direwolf20.buildinggadgets.common.tiles;

import com.direwolf20.buildinggadgets.common.containers.ChargingStationContainer;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargingStationTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {


    public static final int SIZE = 2;
    private static final int FUEL_SLOT = 0;
    private static final int CHARGE_SLOT = 1;
    private int counter;

    private final IEnergyStorage energy;
    private final LazyOptional<IEnergyStorage> energyCap;

    public ChargingStationTileEntity() {
        super(BGBlocks.BGTileEntities.CHARGING_STATION_TYPE);
        energy = new EnergyStorage(10000); //TODO config
        energyCap = LazyOptional.of(this::getEnergy);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        Preconditions.checkArgument(getWorld() != null);
        return new ChargingStationContainer(i, getWorld(), pos, playerInventory, playerEntity);
    }

    // This item handler will hold our inventory slots
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
            ChargingStationTileEntity.this.markDirty();
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot == FUEL_SLOT && GadgetUtils.getItemBurnTime(stack) <= 0)
                return stack;
            else if (slot == CHARGE_SLOT && (! stack.getCapability(CapabilityEnergy.ENERGY).isPresent() || getStackInSlot(slot).getCount() > 0))
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    };

    @Nonnull
    private IEnergyStorage getEnergy() {
        return energy;
    }

    @Nonnull
    private ItemStackHandler getItemStackHandler() {
        return itemStackHandler;
    }

    private ItemStack getChargeStack() {
        return getItemStackHandler().getStackInSlot(CHARGE_SLOT);
    }

    private ItemStack getFuelStack() {
        return getItemStackHandler().getStackInSlot(FUEL_SLOT);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS))
            itemStackHandler.deserializeNBT(compound.getCompound(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS));
        energy.extractEnergy(Integer.MAX_VALUE, false);
        energy.receiveEnergy(compound.getInt(NBTKeys.ENERGY), false);
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS, itemStackHandler.serializeNBT());
        compound.putInt(NBTKeys.ENERGY, energy.extractEnergy(Integer.MAX_VALUE, true));
        return super.write(compound);
    }


    public boolean canInteractWith(PlayerEntity playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return !isRemoved() && playerIn.getDistanceSq(new Vec3d(pos).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.of(() -> itemStackHandler).cast();
        if (cap == CapabilityEnergy.ENERGY)
            return energyCap.cast();
        return super.getCapability(cap, side);
    }

    public int addEnergy(int amount) {
        return energy.receiveEnergy(amount, false);
    }

    public ItemStack getRenderStack() {
        LazyOptional<IItemHandler> handler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        LazyOptional<ItemStack> stack = handler.map(h -> h.getStackInSlot(1));
        return stack.orElse(ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        if (getWorld() != null && ! getWorld().isRemote) {
            if (counter > 0) {
                addEnergy(300);
                counter--;
            } else {
                ItemStack stack = getFuelStack();
                int burnTime = GadgetUtils.getItemBurnTime(stack);
                if (burnTime > 0 && getEnergy().getEnergyStored() < getEnergy().getMaxEnergyStored()) {
                    getItemStackHandler().extractItem(0, 1, false);
                    counter = (int) Math.floor(burnTime / 20);
                    addEnergy(300);
                    counter--;
                }
            }
            ItemStack stack = getChargeStack();
            if (! stack.isEmpty()) {
                IEnergyStorage energy = CapabilityUtil.EnergyUtil.getCap(stack).orElseThrow(CapabilityNotPresentException::new);
                if (getEnergy().getEnergyStored() > 0 && energy.getEnergyStored() < energy.getMaxEnergyStored())
                    getEnergy().extractEnergy(energy.receiveEnergy(getEnergy().extractEnergy(Integer.MAX_VALUE, true), false), false);
            }
        }
    }
}
