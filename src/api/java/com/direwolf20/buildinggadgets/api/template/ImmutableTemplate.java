package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.direwolf20.buildinggadgets.api.template.building.BlockData;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.building.ITemplateView;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.longs.Long2ShortAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public final class ImmutableTemplate implements ITemplate {
    private final BlockPos translation;
    private final Long2ShortMap posToStateId;
    private final Short2ObjectMap<BlockData> idToData;
    private final Multiset<IUniqueItem> requiredItems;

    public static ImmutableTemplate create() { //TODO here until this can be implemented properly
        return new ImmutableTemplate();
    }

    private ImmutableTemplate(BlockPos translation, Long2ShortMap posToStateId, Short2ObjectMap<BlockData> idToData, Multiset<IUniqueItem> requiredItems) {
        this.translation = translation;
        this.posToStateId = posToStateId;
        this.idToData = idToData;
        this.requiredItems = requiredItems;
    }

    private ImmutableTemplate() {
        this(BlockPos.ORIGIN, new Long2ShortAVLTreeMap(), new Short2ObjectAVLTreeMap<>(), ImmutableMultiset.of());
    }

    @Nullable
    @Override
    public ITemplateTransaction startTransaction() {
        return null;
    }


    @Override
    public Multiset<IUniqueItem> estimateRequiredItems() {
        return requiredItems;
    }

    @Override
    public ITemplateSerializer getSerializer() {
        return null;
    }

    @Override
    public ITemplateView createViewInContext(IBuildContext buildContext) {
        return null;
    }
}
