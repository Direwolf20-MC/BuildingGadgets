package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.common.util.lang.ITranslationProvider;
import com.direwolf20.buildinggadgets.common.util.lang.MaterialListTranslation;

import java.util.Comparator;

import static com.direwolf20.buildinggadgets.client.gui.materiallist.ScrollingMaterialList.Entry;

enum SortingModes {

    NAME(Comparator.comparing(Entry::getItemName), MaterialListTranslation.BUTTON_SORTING_NAMEAZ),
    NAME_REVERSED(NAME.getComparator().reversed(), MaterialListTranslation.BUTTON_SORTING_NAMEZA),
    REQUIRED(Comparator.comparingInt(Entry::getRequired), MaterialListTranslation.BUTTON_SORTING_REQUIREDACSE),
    REQUIRED_REVERSED(REQUIRED.getComparator().reversed(), MaterialListTranslation.BUTTON_SORTING_MISSINGDESC),
    MISSING(Comparator.comparingInt(Entry::getMissing), MaterialListTranslation.BUTTON_SORTING_MISSINGACSE),
    MISSING_REVERSED(MISSING.getComparator().reversed(), MaterialListTranslation.BUTTON_SORTING_MISSINGDESC);

    private final Comparator<Entry> comparator;
    private final ITranslationProvider translationProvider;

    SortingModes(Comparator<Entry> comparator, ITranslationProvider provider) {
        this.comparator = comparator;
        this.translationProvider = provider;
    }

    public Comparator<Entry> getComparator() {
        return comparator;
    }

    public String getLocalizedName() {
        return translationProvider.format();
    }

    public SortingModes next() {
        int nextIndex = ordinal() + 1;
        return VALUES[nextIndex >= VALUES.length ? 0 : nextIndex];
    }

    public static final SortingModes[] VALUES = values();

}