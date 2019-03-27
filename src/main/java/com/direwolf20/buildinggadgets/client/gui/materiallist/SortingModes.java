package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.common.tools.Sorter.ItemStacks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.BiFunction;

public enum SortingModes {

    NAME(ItemStacks::byName, ItemStacks::byNameInplace, "name", false),
    NAME_REVERSED(ItemStacks::byName, ItemStacks::byNameInplace, "nameReversed", true),
    COUNT(ItemStacks::byCount, ItemStacks::byCountInplace, "count", false),
    COUNT_REVERSED(ItemStacks::byCount, ItemStacks::byCountInplace, "countReversed", true);

    private final BiFunction<List<ItemStack>, Boolean, List<ItemStack>> sorter;
    private final BiFunction<List<ItemStack>, Boolean, List<ItemStack>> inplaceSorter;
    private final String translationKey;
    private final boolean reverse;

    SortingModes(BiFunction<List<ItemStack>, Boolean, List<ItemStack>> sorter, BiFunction<List<ItemStack>, Boolean, List<ItemStack>> inplaceSorter, String translationKey, boolean reverse) {
        this.sorter = sorter;
        this.inplaceSorter = inplaceSorter;
        this.translationKey = "gui.buildinggadgets.materialList.button.sortingMode." + translationKey;
        this.reverse = reverse;
    }

    public List<ItemStack> sort(List<ItemStack> unsorted) {
        return sorter.apply(unsorted, reverse);
    }

    public void sortInplace(List<ItemStack> unsorted) {
        inplaceSorter.apply(unsorted, reverse);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getLocalizedName() {
        return I18n.format(translationKey);
    }

    public SortingModes next() {
        int nextIndex = ordinal() + 1;
        return values()[nextIndex >= values().length ? 0 : nextIndex];
    }

}
