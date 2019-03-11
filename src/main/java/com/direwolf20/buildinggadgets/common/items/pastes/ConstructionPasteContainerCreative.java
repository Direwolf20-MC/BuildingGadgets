package com.direwolf20.buildinggadgets.common.items.pastes;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionPasteContainerCreative extends GenericPasteContainer {

    public ConstructionPasteContainerCreative() {
        super("constructionpastecontainercreative");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        list.add(TextFormatting.WHITE + I18n.format("tooltip.pasteContainer.creative.amountMsg"));
    }

    @Override
    public void setPasteCount(ItemStack stack, int amount) {
    }

    @Override
    public int getPasteCount(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxCapacity() {
        return Integer.MAX_VALUE;
    }

}
