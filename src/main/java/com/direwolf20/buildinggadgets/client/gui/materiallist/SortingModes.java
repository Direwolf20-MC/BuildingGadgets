package com.direwolf20.buildinggadgets.client.gui.materiallist;

import net.minecraft.client.resources.I18n;

import java.util.Comparator;
import java.util.List;

import static com.direwolf20.buildinggadgets.client.gui.materiallist.ScrollingMaterialList.Entry;

enum SortingModes {

    NAME(Comparator.comparing(Entry::getItemName), "name"),
    NAME_REVERSED(NAME.getComparator().reversed(), "nameReversed"),
    REQUIRED(Comparator.comparingInt(Entry::getRequired), "required"),
    REQUIRED_REVERSED(REQUIRED.getComparator().reversed(), "requiredReversed"),
    MISSING(Comparator.comparingInt(Entry::getRequired), "missing"),
    MISSING_REVERSED(MISSING.getComparator().reversed(), "missingReversed");

    private final Comparator<Entry> comparator;
    private final String translationKey;

    SortingModes(Comparator<Entry> comparator, String translationKey) {
        this.comparator = comparator;
        this.translationKey = "gui.buildinggadgets.materialList.button.sorting." + translationKey;
    }

    public void sortInplace(List<Entry> unsorted) {
        unsorted.sort(comparator);
    }

    public Comparator<Entry> getComparator() {
        return comparator;
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