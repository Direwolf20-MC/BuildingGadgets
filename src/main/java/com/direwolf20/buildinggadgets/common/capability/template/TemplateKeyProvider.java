package com.direwolf20.buildinggadgets.common.capability.template;

import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TemplateKeyProvider implements ICapabilityProvider {
    private final LazyOptional<ITemplateKey> lazyOpt;

    public TemplateKeyProvider(ItemStack stack) {
        ItemTemplateKey key = new ItemTemplateKey(stack);
        lazyOpt = LazyOptional.of(() -> key);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityTemplate.TEMPLATE_KEY_CAPABILITY)
            return lazyOpt.cast();
        return LazyOptional.empty();
    }
}
