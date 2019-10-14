package com.direwolf20.buildinggadgets.api.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Allows a simple nbt-data compression to happen, by building a map from the Data to int's.
 * Every call to {@link #applyAsInt(Object)} will return the int key associated with the given value,
 * which is evaluated if the value is at that point unknown. This int key can then be used to efficiently
 * represent the value, which will obviously reduce the size for duplicate values.
 * <p>
 * The mapping build can be represented efficiently using a {@link ListNBT NBTList}. For reading this
 * and reversing the mapping use {@link DataDecompressor}.
 * <p>
 * This class implements {@link ToIntFunction} for cases, where another int mapping may be used sometimes.
 * For example if the Data is associated with an {@link net.minecraftforge.registries.IForgeRegistry} and the
 * integer id's might be used, it is of course more efficient to use those directly.
 *
 * @param <T> The type of Data which is compressed
 * @see DataDecompressor
 */
public final class DataCompressor<T> implements ToIntFunction<T> {
    private int cur;
    private Object2IntMap<T> objectMap;
    private List<T> reverseMap;

    public DataCompressor(int expectedSize) {
        cur = 0;
        objectMap = new Object2IntOpenHashMap<>(expectedSize);
        reverseMap = new LinkedList<>();
    }

    public DataCompressor() {
        cur = 0;
        objectMap = new Object2IntOpenHashMap<>();
        reverseMap = new LinkedList<>();
    }

    @Override
    public int applyAsInt(T value) {
        if (objectMap.containsKey(value))
            return objectMap.getInt(value);
        objectMap.put(value, cur);
        reverseMap.add(value);
        return cur++;
    }

    public List<T> getReverseMap() {
        return reverseMap;
    }

    public ListNBT write(Function<T, ? extends INBT> serializer) {
        ListNBT resList = new ListNBT();
        for (T entry : reverseMap) {
            resList.add(serializer.apply(entry));
        }
        return resList;
    }
}
