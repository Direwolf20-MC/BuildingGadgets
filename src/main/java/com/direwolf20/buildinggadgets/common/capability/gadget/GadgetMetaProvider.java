package com.direwolf20.buildinggadgets.common.capability.gadget;

import com.direwolf20.buildinggadgets.common.capability.template.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.capability.energy.ItemEnergyForge;
import com.direwolf20.buildinggadgets.common.capability.template.ItemTemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.IntSupplier;

/**
 * Capability Provider Designed for all the Gadgets with optional support for
 * the Template Key Capability.
 */
public class GadgetMetaProvider implements ICapabilityProvider {
    private final LazyOptional<ItemEnergyForge> energyCapability;
    private final LazyOptional<ITemplateKey> templateKeyCapability;
    private final LazyOptional<IGadgetMeta> gadgetMetaCapability;

    private final boolean hasTemplateKeySupport;

    public GadgetMetaProvider(ItemStack stack, IntSupplier energyCapacity, boolean hasTemplateKeySupport) {
        this.energyCapability = LazyOptional.of(() -> new ItemEnergyForge(stack,energyCapacity));
        this.gadgetMetaCapability = LazyOptional.of(() -> new GadgetMeta(stack));
        this.templateKeyCapability = hasTemplateKeySupport ? LazyOptional.of(() -> new ItemTemplateKey(stack)) : LazyOptional.empty();
        this.hasTemplateKeySupport = hasTemplateKeySupport;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyCapability.cast();
        }

        if (cap == CapabilityTemplate.TEMPLATE_KEY_CAPABILITY && this.hasTemplateKeySupport) {
            return templateKeyCapability.cast();
        }

        if (cap == OurCapabilities.GADGET_META) {
            return gadgetMetaCapability.cast();
        }

        return LazyOptional.empty();
    }
}
