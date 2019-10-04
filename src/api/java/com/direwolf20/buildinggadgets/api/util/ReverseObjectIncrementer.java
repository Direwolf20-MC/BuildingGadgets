package com.direwolf20.buildinggadgets.api.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.function.Function;
import java.util.function.IntFunction;

public class ReverseObjectIncrementer<T> implements IntFunction<T> {
    private Int2ObjectMap<T> int2ObjectMap;
    private IntFunction<T> defaultFun;

    public ReverseObjectIncrementer(ListNBT list, Function<INBT, T> deserializer, IntFunction<T> defaultFun) {
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
