package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.capability.provider.TemplateKeyProvider;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

public class TemplateItem extends Item {

    public TemplateItem() {
        super(OurItems.itemProperties().maxStackSize(1));
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

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if( !playerIn.isSneaking() )
            return super.onItemRightClick(worldIn, playerIn, handIn);

        return GuiMod.MATERIAL_LIST.openScreen(playerIn)
                ? ActionResult.resultSuccess(playerIn.getHeldItem(handIn))
                : super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public static ItemStack getTemplateItem(PlayerEntity player) {
        ItemStack mainhand = player.getHeldItemMainhand();
        if (mainhand.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent())
            return mainhand;

        ItemStack offhand = player.getHeldItemOffhand();
        if (offhand.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent())
            return offhand;

        return ItemStack.EMPTY;
    }
}
