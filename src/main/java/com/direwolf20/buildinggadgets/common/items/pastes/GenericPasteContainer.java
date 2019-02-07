package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public abstract class GenericPasteContainer extends Item {

    public GenericPasteContainer() {
        setCreativeTab(BuildingGadgets.BUILDING_CREATIVE_TAB);
        setMaxStackSize(1);
    }

    /**
     * Helper method. Delegates to {@link GenericPasteContainer#setPastes(ItemStack, int)}.
     */
    public static void setPasteAmount(ItemStack stack, int amount) {
        Item item = stack.getItem();
        if (item instanceof GenericPasteContainer) {
            ((GenericPasteContainer) item).setPastes(stack, amount);
        } else {
            BuildingGadgets.logger.warn("Potential abuse of GenericPasteContainer#setPasteAmount(ItemStack, int) where the given ItemStack does not contain a GenericPasteContainer.");
        }
    }

    /**
     * Helper method. Delegates to {@link GenericPasteContainer#getPastes(ItemStack)}}.
     */
    public static int getPasteAmount(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof GenericPasteContainer) {
            return ((GenericPasteContainer) item).getPastes(stack);
        } else {
            BuildingGadgets.logger.warn("Potential abuse of GenericPasteContainer#getPasteAmount(ItemStack) where the given ItemStack does not contain a GenericPasteContainer.");
            return 0;
        }
    }

    protected static String getAmountDisplayLocalized() {
        return TextFormatting.WHITE + I18n.format("tooltip.pasteContainer.amount");
    }

    public abstract void setPastes(ItemStack stack, int amount);

    public abstract int getPastes(ItemStack stack);

    public abstract int getMaxAmount();

}
