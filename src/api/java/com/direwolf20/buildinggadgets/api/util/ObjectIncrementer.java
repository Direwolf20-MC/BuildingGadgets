package com.direwolf20.buildinggadgets.api.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class ObjectIncrementer<T> implements ToIntFunction<T> {
    private int cur;
    private Object2IntMap<T> objectMap;
    private List<T> reverseMap;

    public ObjectIncrementer(int expectedSize) {
        cur = 0;
        objectMap = new Object2IntOpenHashMap<>(expectedSize);
        reverseMap = new LinkedList<>();
    }

    public ObjectIncrementer() {
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
