package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.capability.provider.TemplateKeyProvider;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

public class TemplateItem extends Item {

    public TemplateItem() {
        super(OurItems.itemProperties().stacksTo(1));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new TemplateKeyProvider(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        GadgetUtils.addTooltipNameAndAuthor(stack, worldIn, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if( !playerIn.isShiftKeyDown() )
            return super.use(worldIn, playerIn, handIn);

        if (worldIn.isClientSide) {
            return GuiMod.MATERIAL_LIST.openScreen(playerIn)
                    ? InteractionResultHolder.success(playerIn.getItemInHand(handIn))
                    : super.use(worldIn, playerIn, handIn);
        }

        return super.use(worldIn, playerIn, handIn);
    }

    public static ItemStack getTemplateItem(Player player) {
        ItemStack mainhand = player.getMainHandItem();
        if (mainhand.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent())
            return mainhand;

        ItemStack offhand = player.getOffhandItem();
        if (offhand.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent())
            return offhand;

        return ItemStack.EMPTY;
    }
}
