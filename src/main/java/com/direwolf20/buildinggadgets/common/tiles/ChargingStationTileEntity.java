package com.direwolf20.buildinggadgets.common.tiles;

import com.direwolf20.buildinggadgets.common.capability.ConfigEnergyStorage;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.containers.ChargingStationContainer;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Preconditions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
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
    private static final int UPDATE_FLAG_INVENTORY = 2;
    private static final int UPDATE_FLAG_ENERGY = 1;
    private static final int UPDATE_FLAG_ALL = UPDATE_FLAG_INVENTORY | UPDATE_FLAG_ENERGY;
    private final int SEND_UPDATE_NO_RENDER = BlockFlags.NOTIFY_LISTENERS | BlockFlags.NO_RERENDER;
    private int updateNeeded;
    private int counter;
    private int renderCounter = 0;

    private final ConfigEnergyStorage energy;
    private final ItemStackHandler itemStackHandler;
    private final LazyOptional<IEnergyStorage> energyCap;
    private final LazyOptional<IItemHandler> itemCap;

    public ChargingStationTileEntity() {
        super(BGBlocks.BGTileEntities.CHARGING_STATION_TYPE);
        energy = new ConfigEnergyStorage(Config.CHARGING_STATION.capacity::get) {
            @Override
            protected void writeEnergy() {
                ChargingStationTileEntity.this.markDirty();
                updateNeeded |= UPDATE_FLAG_ENERGY;
                if (getWorld() != null && ! getWorld().isRemote()) //TODO this is unnecessary overhead: replace with custom update packet and update System... Similar to DataManger
                    getWorld().notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), SEND_UPDATE_NO_RENDER);
            }

            @Override
            protected void updateEnergy() {

            }
        };
        itemStackHandler = new ItemStackHandler(SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                ChargingStationTileEntity.this.markDirty();
                updateNeeded |= UPDATE_FLAG_INVENTORY;
                if (getWorld() != null && ! getWorld().isRemote()) //TODO more efficient update System - see energy...s
                    getWorld().notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), SEND_UPDATE_NO_RENDER);
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
        energyCap = LazyOptional.of(this::getEnergy);
        itemCap = LazyOptional.of(this::getItemStackHandler);
        updateNeeded = UPDATE_FLAG_ALL;
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

    @Nonnull
    private ConfigEnergyStorage getEnergy() {
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
    @Nonnull
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        if (updateNeeded == 0)
            return null;
        CompoundNBT nbtTag = new CompoundNBT();
        if ((updateNeeded & UPDATE_FLAG_ENERGY) == UPDATE_FLAG_ENERGY)
            writeEnergyNBT(nbtTag);
        if ((updateNeeded & UPDATE_FLAG_INVENTORY) == UPDATE_FLAG_INVENTORY)
            writeItemNBT(nbtTag);
        SUpdateTileEntityPacket packet = new SUpdateTileEntityPacket(getPos(), updateNeeded, nbtTag);
        updateNeeded = 0;
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        // Here we get the packet from the server and read it into our client side tile entity
        CompoundNBT nbt = packet.getNbtCompound();
        boolean causeReRender = false; //required to update the render when items are inserted/extracted by a hopper for example
        if ((packet.getTileEntityType() & UPDATE_FLAG_INVENTORY) == UPDATE_FLAG_INVENTORY) {
            ItemStack renderOld = getRenderStack();
            readItemNBT(nbt);
            if (! renderOld.equals(getRenderStack(), false) && getWorld() != null)
                causeReRender = true;
        }
        if ((packet.getTileEntityType() & UPDATE_FLAG_ENERGY) == UPDATE_FLAG_ENERGY)
            readEnergyNBT(nbt);
        if (causeReRender) //implemented this way in order allow future expansion, when the render influences more things
            getWorld().notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), BlockFlags.NOTIFY_LISTENERS);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        readItemNBT(compound);
        readEnergyNBT(compound);
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT compound) {
        writeItemNBT(compound);
        readEnergyNBT(compound);
        return super.write(compound);
    }

    private void writeItemNBT(CompoundNBT compound) {
        compound.put(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS, itemStackHandler.serializeNBT());
    }

    private void writeEnergyNBT(CompoundNBT compound) {
        compound.putInt(NBTKeys.ENERGY, energy.getEnergyStored());
    }

    private void readItemNBT(CompoundNBT compound) {
        if (compound.contains(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS))
            itemStackHandler.deserializeNBT(compound.getCompound(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS));
    }

    private void readEnergyNBT(CompoundNBT compound) {
        if (compound.contains(NBTKeys.ENERGY))
            getEnergy().setEnergy(compound.getInt(NBTKeys.ENERGY));
    }

    public boolean canInteractWith(PlayerEntity playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return ! isRemoved() && playerIn.getDistanceSq(new Vec3d(getPos()).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return itemCap.cast();
        if (cap == CapabilityEnergy.ENERGY)
            return energyCap.cast();
        return super.getCapability(cap, side);
    }

    private void addEnergy(int amount) {
        energy.receiveEnergy(amount, false);
    }

    public ItemStack getRenderStack() {
        return getChargeStack();
    }

    @Override
    public void tick() {
        if (getWorld() != null && ! getWorld().isRemote) {
            if (counter > 0 && getEnergy().receiveEnergy(Config.CHARGING_STATION.energyPerTick.get(), true) > 0) {
                addEnergy(Config.CHARGING_STATION.energyPerTick.get());
                counter--;
            } else {
                ItemStack stack = getFuelStack();
                int burnTime = GadgetUtils.getItemBurnTime(stack);
                if (burnTime > 0 && getEnergy().receiveEnergy(Config.CHARGING_STATION.energyPerTick.get(), true) > 0) {
                    getItemStackHandler().extractItem(0, 1, false);
                    counter = (int) Math.floor(burnTime / Config.CHARGING_STATION.fuelUsage.get());
                    addEnergy(Config.CHARGING_STATION.energyPerTick.get());
                    counter--;
                }
            }
            ItemStack stack = getChargeStack();
            if (! stack.isEmpty()) {
                IEnergyStorage energy = CapabilityUtil.EnergyUtil.getCap(stack).orElseThrow(CapabilityNotPresentException::new);
                if (getEnergy().getEnergyStored() > 0 && energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                    getEnergy().extractEnergy(energy.receiveEnergy(getEnergy().extractEnergy(Config.CHARGING_STATION.chargePerTick.get(), true), false), false);
                    //Every second, when charging an item, send a sync packet to the client so it knows how far along it is for the render coloring
                    if (renderCounter % 20 == 0) {
                        BlockState state = getWorld().getBlockState(getPos());
                        getWorld().notifyBlockUpdate(getPos(), state, state, BlockFlags.NOTIFY_LISTENERS);
                    }
                    renderCounter++;
                }
            }
        }
    }
}
