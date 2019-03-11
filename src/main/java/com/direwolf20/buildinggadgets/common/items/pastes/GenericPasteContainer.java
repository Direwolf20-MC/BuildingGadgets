package com.direwolf20.buildinggadgets.common.items.pastes;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.ItemModBase;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public abstract class GenericPasteContainer extends ItemModBase {

    public GenericPasteContainer(String name) {
        super(name);
        setMaxStackSize(1);
    }

    /**
     * Helper method. Delegates to {@link GenericPasteContainer#setPasteCount(ItemStack, int)}.
     */
    public static void setPasteAmount(ItemStack stack, int amount) {
        Item item = stack.getItem();
        if (item instanceof GenericPasteContainer) {
            ((GenericPasteContainer) item).setPasteCount(stack, amount);
        } else {
            BuildingGadgets.logger.warn("Potential abuse of GenericPasteContainer#setPasteAmount(ItemStack, int) where the given ItemStack does not contain a GenericPasteContainer.");
        }
    }

    /**
     * Helper method. Delegates to {@link GenericPasteContainer#getPasteCount(ItemStack)}}.
     */
    public static int getPasteAmount(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof GenericPasteContainer) {
            return ((GenericPasteContainer) item).getPasteCount(stack);
        }
        BuildingGadgets.logger.warn("Potential abuse of GenericPasteContainer#getPasteAmount(ItemStack) where the given ItemStack does not contain a GenericPasteContainer.");
        return 0;
    }

    protected static String getAmountDisplayLocalized() {
        return TextFormatting.WHITE + I18n.format("tooltip.pasteContainer.amount");
    }

    /**
     * Set and store the amount of construction pastes in item nbt. Additionally it will clamp the parameter between {@link
     * #getMaxCapacity()} and 0 inclusively.
     */
    public abstract void setPasteCount(ItemStack stack, int amount);

    /**
     * Read and return the amount of construction pastes in item nbt. Always lower or equal to {@link #getMaxCapacity()}.
     */
    public abstract int getPasteCount(ItemStack stack);

    /**
     * @return maximum number of construction paste the container variant can hold.
     */
    public abstract int getMaxCapacity();

}
