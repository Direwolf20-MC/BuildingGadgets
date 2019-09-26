package com.direwolf20.buildinggadgets.common.util.inventory;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.util.inventory.SimpleItemIndex.MatchResult;
import com.google.common.collect.Multimap;
import net.minecraft.item.Item;

import java.util.Set;

public final class SimpleItemIndex implements IItemIndex<MatchResult> {
    private final Multimap<Item, IStackHandle> handleMap;
    private final Set<IInsertExtractProvider> insertExtractProviders;

    SimpleItemIndex(Multimap<Item, IStackHandle> handleMap, Set<IInsertExtractProvider> insertExtractProviders) {
        this.handleMap = handleMap;
        this.insertExtractProviders = insertExtractProviders;
    }

    @Override
    public MatchResult tryMatch(MaterialList list) {
        return null;
    }

    @Override
    public boolean applyMatch(MatchResult result) {
        return false;
    }

    public static final class MatchResult implements IMatchResult {
        private final boolean isSuccess;

        public MatchResult(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        @Override
        public boolean isSuccess() {
            return isSuccess;
        }
    }
}
