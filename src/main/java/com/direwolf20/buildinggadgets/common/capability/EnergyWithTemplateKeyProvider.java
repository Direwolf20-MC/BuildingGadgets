package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.IntSupplier;

public class EnergyWithTemplateKeyProvider extends CapabilityProviderEnergy {
    private final LazyOptional<ItemEnergyForge> energyCapability;
    private final LazyOptional<ITemplateKey> templateKeyCapability;

    public EnergyWithTemplateKeyProvider(ItemStack stack, IntSupplier energyCapacity) {
        super(stack, energyCapacity);

        this.energyCapability = LazyOptional.of(() -> new ItemEnergyForge(stack,energyCapacity));
        templateKeyCapability = LazyOptional.of(() -> new ItemTemplateKey(stack));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyCapability.cast();
        }

        if (cap == CapabilityTemplate.TEMPLATE_KEY_CAPABILITY) {
            return templateKeyCapability.cast();
        }

        return LazyOptional.empty();
    }
}
