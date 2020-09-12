package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.collect.Multiset;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Tuple;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

/**
 * Utility class providing additional Methods for reading and writing array's which are not normally provided as
 * NBT-Objects from Minecraft.
 */

@Tainted(reason = "Everything here is single use. It should not be a helper")
public class NBTHelper {
    public static <T> ListNBT writeIterable(Iterable<T> iterable, Function<? super T, ? extends INBT> serializer) {
        return StreamSupport.stream(iterable.spliterator(), false).map(serializer).collect(toListNBT());
    }

    public static <K, V> ListNBT serializeMap(Map<K, V> map, Function<? super K, ? extends INBT> keySerializer, Function<? super V, ? extends INBT> valueSerializer) {
        ListNBT list = new ListNBT();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            CompoundNBT compound = new CompoundNBT();
            compound.put(NBTKeys.MAP_SERIALIZE_KEY, keySerializer.apply(entry.getKey()));
            compound.put(NBTKeys.MAP_SERIALIZE_VALUE, valueSerializer.apply(entry.getValue()));
            list.add(compound);
        }
        return list;
    }

    public static <V> ListNBT serializeUUIDMap(Map<UUID, V> map, Function<? super V, ? extends INBT> valueSerializer) {
        ListNBT list = new ListNBT();
        for (Map.Entry<UUID, V> entry : map.entrySet()) {
            CompoundNBT compound = new CompoundNBT();
            compound.putUniqueId(NBTKeys.MAP_SERIALIZE_KEY, entry.getKey());
            compound.put(NBTKeys.MAP_SERIALIZE_VALUE, valueSerializer.apply(entry.getValue()));
            list.add(compound);
        }
        return list;
    }

    public static <K, V> Map<K, V> deserializeMap(ListNBT list, Map<K, V> toAppendTo, Function<INBT, ? extends K> keyDeserializer, Function<INBT, ? extends V> valueDeserializer) {
        for (INBT nbt : list) {
            if (nbt instanceof CompoundNBT) {
                CompoundNBT compound = (CompoundNBT) nbt;
                toAppendTo.put(
                        keyDeserializer.apply(compound.get(NBTKeys.MAP_SERIALIZE_KEY)),
                        valueDeserializer.apply(compound.get(NBTKeys.MAP_SERIALIZE_VALUE))
                );
            }
        }
        return toAppendTo;
    }

    public static <V> Map<UUID, V> deserializeUUIDMap(ListNBT list, Map<UUID, V> toAppendTo, Function<INBT, ? extends V> valueDeserializer) {
        for (INBT nbt : list) {
            if (nbt instanceof CompoundNBT) {
                CompoundNBT compound = (CompoundNBT) nbt;
                toAppendTo.put(
                        compound.getUniqueId(NBTKeys.MAP_SERIALIZE_KEY),
                        valueDeserializer.apply(compound.get(NBTKeys.MAP_SERIALIZE_VALUE))
                );
            }
        }
        return toAppendTo;
    }

    public static <T, C extends Collection<T>> C deserializeCollection(ListNBT list, C toAppendTo, Function<INBT, ? extends T> elementDeserializer) {
        for (INBT nbt : list) {
            toAppendTo.add(elementDeserializer.apply(nbt));
        }
        return toAppendTo;
    }

    public static <T> Multiset<T> deserializeMultisetEntries(ListNBT list, Multiset<T> toAppendTo, Function<INBT, Tuple<? extends T, Integer>> entryDeserializer) {
        list.stream().map(entryDeserializer).forEach(p -> toAppendTo.add(p.getA(), p.getB()));
        return toAppendTo;
    }

    public static <T extends INBT> Collector<T, ListNBT, ListNBT> toListNBT() {
        return new Collector<T, ListNBT, ListNBT>() {
            @Override
            public Supplier<ListNBT> supplier() {
                return ListNBT::new;
            }

            @Override
            public BiConsumer<ListNBT, T> accumulator() {
                return ListNBT::add;
            }

            @Override
            public BinaryOperator<ListNBT> combiner() {
                return (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                };
            }

            @Override
            public Function<ListNBT, ListNBT> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.of(Characteristics.IDENTITY_FINISH, Characteristics.UNORDERED);
            }
        };
    }

}
