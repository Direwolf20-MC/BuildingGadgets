package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.APIProxy;
import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.longs.Long2ShortAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    @Override
    public Stream<PlacementTarget> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Nullable
    @Override
    public ITemplateTransaction startTransaction() {
        return null;
    }

    @Override
    public ImmutableTemplate translateTo(BlockPos pos) {
        return new ImmutableTemplate(pos, posToStateId, idToData, requiredItems);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     * @return an Iterator.
     */
    @Override
    public Iterator<PlacementTarget> iterator() {

        return new AbstractIterator<PlacementTarget>() {
            @Override
            protected PlacementTarget computeNext() {
                return null;
            }
        };
    }

    @Override
    public Multiset<IUniqueItem> getRequiredItems() {
        return requiredItems;
    }

    @Override
    public int estimateSize() {
        return 0;
    }
}
