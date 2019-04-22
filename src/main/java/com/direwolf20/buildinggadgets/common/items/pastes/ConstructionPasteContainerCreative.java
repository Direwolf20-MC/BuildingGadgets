package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ConstructionPasteContainerCreative extends GenericPasteContainer {
    public ConstructionPasteContainerCreative(Properties builder) {
        super(builder);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        list.add(TooltipTranslation.PASTECONTAINER_CREATIVE_AMOUNT.componentTranslation().setStyle(Styles.WHITE));
    }

    @Override
    public void setPasteCount(ItemStack stack, int amount) {}

    @Override
    public int getPasteCount(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxCapacity() {
        return Integer.MAX_VALUE;
    }

}
