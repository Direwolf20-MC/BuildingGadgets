package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.capability.OurCapabilities;
import com.direwolf20.buildinggadgets.common.capability.template.TemplateKeyProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class TemplateItem extends Item {
    public TemplateItem() {
        super(new Properties());
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new TemplateKeyProvider(stack);
    }

    public static ItemStack getFromHand(PlayerEntity player) {
        ItemStack mainhand = player.getHeldItemMainhand();
        if (mainhand.getCapability(OurCapabilities.TEMPLATE_KEY_CAPABILITY).isPresent())
            return mainhand;

        ItemStack offhand = player.getHeldItemOffhand();
        if (offhand.getCapability(OurCapabilities.TEMPLATE_KEY_CAPABILITY).isPresent())
            return offhand;

        return ItemStack.EMPTY;
    }
}
