package com.direwolf20.buildinggadgets.common.util.compression;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Allows to read data compressed to integers by {@link DataCompressor}.
 * Every call to {@link #apply(int)} will lookup the appropriate value from the compressed data which
 * was read in the constructor.
 * <p>
 * This class implements {@link IntFunction} for cases, where another int mapping may be used sometimes.
 * For example if the Data is associated with an {@link net.minecraftforge.registries.IForgeRegistry} and the
 * integer id's might be used, it is of course more efficient to use those directly.
 *
 * @param <T> The type of Data to decompress
 * @see DataCompressor
 */
public class DataDecompressor<T> implements IntFunction<T> {
    private Int2ObjectMap<T> int2ObjectMap;
    private IntFunction<T> defaultFun;

    public DataDecompressor(ListNBT list, Function<INBT, T> deserializer, IntFunction<T> defaultFun) {
        int2ObjectMap = new Int2ObjectOpenHashMap<>(list.size());
        this.defaultFun = defaultFun;
        int count = 0;
        for (INBT entry : list) {
            int2ObjectMap.put(count++, deserializer.apply(entry));
        }
    }

    @Override
    public T apply(int value) {
        if (! int2ObjectMap.containsKey(value))
            return defaultFun.apply(value);
        return int2ObjectMap.get(value);
    }
}
