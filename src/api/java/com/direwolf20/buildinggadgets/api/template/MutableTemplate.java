package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.google.common.collect.Multiset;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

public class MutableTemplate implements ITemplate {
    private ImmutableTemplate delegate;

    public MutableTemplate(ImmutableTemplate delegate) {
        this.delegate = delegate;
    }

    public MutableTemplate() {
        this(ImmutableTemplate.create());
    }

    @Override
    public Stream<PlacementTarget> stream() {
        return delegate.stream();
    }

    @Override
    @Nullable
    public ITemplateTransaction startTransaction() {
        return delegate.startTransaction();
    }

    @Override
    public boolean translateTo(BlockPos pos) {
        delegate = delegate.withTranslation(pos);
        return true;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     * @return an Iterator.
     */
    @Override
    public Iterator<PlacementTarget> iterator() {
        return delegate.iterator();
    }

    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    public void deserializeNBT(NBTTagCompound nbt) {

    }

    @Override
    public Multiset<IUniqueItem> getRequiredItems() {
        return delegate.getRequiredItems();
    }

    public ImmutableTemplate withTranslation(BlockPos pos) {
        return delegate.withTranslation(pos);
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return delegate.spliterator();
    }
}
