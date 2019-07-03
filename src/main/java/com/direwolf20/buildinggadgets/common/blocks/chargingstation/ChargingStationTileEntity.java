package com.direwolf20.buildinggadgets.common.blocks.chargingstation;

import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class ChargingStationTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IEnergyStorage {

    private static final Set<Item> allowedItemsLeft = ImmutableSet.of(Items.COAL, Items.CHARCOAL);
    private static final Set<Item> allowedItemsRight = ImmutableSet.of(BGItems.gadgetCopyPaste, BGItems.gadgetBuilding, BGItems.gadgetDestruction, BGItems.gadgetExchanger);

    public static final int SIZE = 2;

    private final int capacity = 1000000;
    private int counter;

    private LazyOptional<IEnergyStorage> energy = LazyOptional.of(this::createEnergy);

    public ChargingStationTileEntity() {
        super(BGBlocks.BGTileEntities.CHARGING_STATION_TYPE);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return this.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }


    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ChargingStationContainer(i, world, pos, playerInventory, playerEntity);
    }

    // This item handler will hold our inventory slots
    private ItemStackHandler itemStackHandler = new ItemStackHandler(SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the tile entity that something has changed so
            // that the chest contents is persisted
            ChargingStationTileEntity.this.markDirty();
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (slot == 0) {
                if (!(allowedItemsLeft.contains(stack.getItem()))) {
                    return stack;
                }
            } else if (slot == 1) {
                if (!(allowedItemsRight.contains(stack.getItem()))) {
                    return stack;
                }
                if (getStackInSlot(slot).getCount() > 0)
                    return stack;

                if (stack.getCount() > 1) {
                    super.insertItem(slot, ItemHandlerHelper.copyStackWithSize(stack, 1), simulate);
                    return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - 1);
                }
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS))
            itemStackHandler.deserializeNBT(compound.getCompound(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS));
        CompoundNBT energyTag = compound.getCompound("energy");
        energy.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(energyTag));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(NBTKeys.TE_TEMPLATE_MANAGER_ITEMS, itemStackHandler.serializeNBT());
        energy.ifPresent(h -> {
            CompoundNBT tag = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
            compound.put("energy", tag);
        });
        return super.write(compound);
    }

    private IEnergyStorage createEnergy() {
        return new CustomEnergyStorage(capacity, 0);
    }


    public boolean canInteractWith(PlayerEntity playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return !isRemoved() && playerIn.getDistanceSq(new Vec3d(pos).add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> itemStackHandler).cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    public ChargingStationContainer getContainer(PlayerEntity playerIn) {
        return new ChargingStationContainer(0, playerIn.world, this.pos, playerIn.inventory, playerIn);
    }

    public void addEnergy(int amount) {
        energy.ifPresent(e -> ((CustomEnergyStorage) e).addEnergy(amount));
    }

    @Override
    public void tick() {
        LazyOptional<IItemHandler> handler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!world.isRemote) {
            if (counter > 0) {
                addEnergy(300);
                counter--;
            } else {
                handler.ifPresent(h -> {
                    ItemStack stack = h.getStackInSlot(0);
                    int burnTime = net.minecraftforge.event.ForgeEventFactory.getItemBurnTime(stack, stack.getBurnTime() == -1 ? AbstractFurnaceTileEntity.getBurnTimes().getOrDefault(stack.getItem(), 0) : stack.getBurnTime());
                    if (burnTime > 0 && getEnergyStored() < getMaxEnergyStored()) {
                        h.extractItem(0, 1, false);
                        counter = (int) Math.floor(burnTime / 20);
                        addEnergy(300);
                        counter--;
                    }
                });
            }
            handler.ifPresent(h -> {
                ItemStack stack = h.getStackInSlot(1);
                if (!stack.isEmpty()) {
                    IEnergyStorage energy = CapabilityUtil.EnergyUtil.getCap(stack).orElseThrow(CapabilityNotPresentException::new);
                    if (getEnergyStored() > 0 && energy.getEnergyStored() < energy.getMaxEnergyStored()) {
                        addEnergy(energy.receiveEnergy(Math.min(10000, getEnergyStored()), false) * -1);
                    }
                }
            });
        }
    }
}
