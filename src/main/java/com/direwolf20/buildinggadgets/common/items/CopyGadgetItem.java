package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.capability.EnergyWithTemplateKeyProvider;
import com.direwolf20.buildinggadgets.common.config.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class CopyGadgetItem extends AbstractGadget {
    public CopyGadgetItem() {
        super(Config.GADGETS.GADGET_COPY_PASTE);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new EnergyWithTemplateKeyProvider(stack, this.config.maxEnergy::get);
    }
}
