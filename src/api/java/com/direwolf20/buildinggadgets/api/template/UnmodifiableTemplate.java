package com.direwolf20.buildinggadgets.api.template;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ShortAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ShortMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class UnmodifiableTemplate implements ITemplate {
    private BlockPos translation;
    private final Long2ShortMap posToStateId;
    private final Short2ObjectMap<BlockData> idToData;

    public UnmodifiableTemplate() {
        this.translation = BlockPos.ORIGIN;
        posToStateId = new Long2ShortAVLTreeMap();
        idToData = new Short2ObjectAVLTreeMap<>();
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
    public void translateTo(BlockPos pos) {

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
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }
}
