package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.capability.provider.TemplateKeyProvider;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

public class TemplateItem extends Item {

    public TemplateItem(Properties builder) {
        super(builder.maxStackSize(1));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new TemplateKeyProvider(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        GadgetUtils.addTooltipNameAndAuthor(stack, worldIn, tooltip);
    }

}
