package com.direwolf20.buildinggadgets.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PasteContainerCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<PasteContainerItemHandler> itemHandler;

    public PasteContainerCapabilityProvider(ItemStack container) {
        this.itemHandler = LazyOptional.of(() -> new PasteContainerItemHandler(container));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (ForgeCapabilities.ITEM_HANDLER == cap)
            return itemHandler.cast();
        return LazyOptional.empty();
    }
}
