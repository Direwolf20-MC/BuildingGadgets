package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.direwolf20.buildinggadgets.api.template.building.BlockData;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.building.ITemplateView;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2IntAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * An immutable implementation of {@link ITemplate}. It supports {@link ITemplateTransaction}, but those will always create a new instance.
 */
public final class ImmutableTemplate implements ITemplate {
    private final BlockPos translation;
    private final Long2IntMap posToStateId;
    private final Int2ObjectMap<BlockData> idToData;
    private final ImmutableMultiset<IUniqueItem> requiredItems;

    /**
     * @return A new {@code ImmutableTemplate}
     */
    public static ImmutableTemplate create() { //TODO here until this can be implemented properly
        return new ImmutableTemplate();
    }

    private ImmutableTemplate(BlockPos translation, Long2IntMap posToStateId, Int2ObjectMap<BlockData> idToData, Multiset<IUniqueItem> requiredItems) {
        this.translation = translation;
        this.posToStateId = posToStateId;
        this.idToData = idToData;
        this.requiredItems = ImmutableMultiset.copyOf(requiredItems);
    }

    private ImmutableTemplate() {
        this(BlockPos.ORIGIN, new Long2IntAVLTreeMap(), new Int2ObjectAVLTreeMap<>(), ImmutableMultiset.of());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ITemplateTransaction startTransaction() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Multiset<IUniqueItem> estimateRequiredItems() {
        return requiredItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITemplateSerializer getSerializer() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITemplateView createViewInContext(IBuildContext buildContext) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int estimateSize() {
        return posToStateId.size();
    }
}
