package com.direwolf20.buildinggadgets.common.util.helpers;

import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class providing additional Methods for reading and writing array's which are not normally provided as
 * NBT-Objects from Minecraft.
 */
public class NBTHelper {

    public static ByteArrayNBT createBooleanList(boolean[] booleans) {
        byte[] bytes = new byte[booleans.length];
        for (int i = 0; i < booleans.length; ++i) {
            bytes[i] = (byte) (booleans[i] ? 0 : 1);
        }
        return new ByteArrayNBT(bytes);
    }

    public static ByteArrayNBT createBooleanList(Boolean[] booleans) {
        byte[] bytes = new byte[booleans.length];
        for (int i = 0; i < booleans.length; ++i) {
            bytes[i] = (byte) (booleans[i] ? 0 : 1);
        }
        return new ByteArrayNBT(bytes);
    }

    public static ListNBT createShortList(short[] shorts) {
        ListNBT list = new ListNBT();
        for (short s : shorts) {
            list.add(new ShortNBT(s));
        }
        return list;
    }

    public static ListNBT createShortList(Short[] shorts) {
        ListNBT list = new ListNBT();
        for (short s : shorts) {
            list.add(new ShortNBT(s));
        }
        return list;
    }

    @Nonnull
    public static ListNBT createFloatList(float[] floats) {
        ListNBT list = new ListNBT();
        for (float f : floats) {
            list.add(new FloatNBT(f));
        }
        return list;
    }

    @Nonnull
    public static ListNBT createFloatList(Float[] floats) {
        ListNBT list = new ListNBT();
        for (Float f : floats) {
            list.add(new FloatNBT(f));
        }
        return list;
    }

    @Nonnull
    public static ListNBT createDoubleList(double[] doubles) {
        ListNBT list = new ListNBT();
        for (double d : doubles) {
            list.add(new DoubleNBT(d));
        }
        return list;
    }

    @Nonnull
    public static ListNBT createDoubleList(Double[] doubles) {
        ListNBT list = new ListNBT();
        for (Double d : doubles) {
            list.add(new DoubleNBT(d));
        }
        return list;
    }

    @Nonnull
    public static short[] readShortList(ListNBT shorts) {
        short[] res = new short[shorts.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < shorts.size(); i++) {
            INBT nbt = shorts.get(i);
            if (nbt instanceof ShortNBT) {
                res[i] = ((ShortNBT) nbt).getShort();
            } else {
                res[i] = 0;
                failed.add(i);
            }
        }
        if (!failed.isEmpty()) {
            short[] shortened = new short[res.length - failed.size()];
            int shortenedCount = 0;
            for (int i = 0; i < res.length; i++) {
                if (failed.contains(i))
                    continue;
                shortened[shortenedCount] = res[i];
                ++shortenedCount;
            }
            res = shortened;
        }
        return res;
    }

    @Nonnull
    public static Short[] readBShortList(ListNBT shorts) {
        Short[] res = new Short[shorts.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < shorts.size(); i++) {
            INBT nbt = shorts.get(i);
            if (nbt instanceof ShortNBT) {
                res[i] = ((ShortNBT) nbt).getShort();
            } else {
                res[i] = 0;
                failed.add(i);
            }
        }
        if (!failed.isEmpty()) {
            Short[] shortened = new Short[res.length - failed.size()];
            int shortenedCount = 0;
            for (int i = 0; i < res.length; i++) {
                if (failed.contains(i))
                    continue;
                shortened[shortenedCount] = res[i];
                ++shortenedCount;
            }
            res = shortened;
        }
        return res;
    }

    @Nonnull
    public static ListNBT createStringList(String[] strings) {
        ListNBT list = new ListNBT();
        for (String s : strings) {
            list.add(new StringNBT(s));
        }
        return list;
    }

    @Nonnull
    public static float[] readFloatList(ListNBT floats) {
        float[] res = new float[floats.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < floats.size(); i++) {
            INBT nbt = floats.get(i);
            if (nbt instanceof FloatNBT) {
                res[i] = ((FloatNBT) nbt).getFloat();
            } else {
                res[i] = 0;
                failed.add(i);
            }
        }
        if (!failed.isEmpty()) {
            float[] shortened = new float[res.length - failed.size()];
            int shortenedCount = 0;
            for (int i = 0; i < res.length; i++) {
                if (failed.contains(i))
                    continue;
                shortened[shortenedCount] = res[i];
                ++shortenedCount;
            }
            res = shortened;
        }
        return res;
    }

    @Nonnull
    public static Float[] readBFloatList(ListNBT floats) {
        Float[] res = new Float[floats.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < floats.size(); i++) {
            INBT nbt = floats.get(i);
            if (nbt instanceof FloatNBT) {
                res[i] = ((FloatNBT) nbt).getFloat();
            } else {
                res[i] = 0f;
                failed.add(i);
            }
        }
        if (!failed.isEmpty()) {
            Float[] shortened = new Float[res.length - failed.size()];
            int shortenedCount = 0;
            for (int i = 0; i < res.length; i++) {
                if (failed.contains(i))
                    continue;
                shortened[shortenedCount] = res[i];
                ++shortenedCount;
            }
            res = shortened;
        }
        return res;
    }

    @Nonnull
    public static double[] readDoubleList(ListNBT doubles) {
        double[] res = new double[doubles.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < doubles.size(); i++) {
            INBT nbt = doubles.get(i);
            if (nbt instanceof DoubleNBT) {
                res[i] = ((DoubleNBT) nbt).getDouble();
            } else {
                res[i] = 0;
                failed.add(i);
            }
        }
        if (!failed.isEmpty()) {
            double[] shortened = new double[res.length - failed.size()];
            int shortenedCount = 0;
            for (int i = 0; i < res.length; i++) {
                if (failed.contains(i))
                    continue;
                shortened[shortenedCount] = res[i];
                ++shortenedCount;
            }
            res = shortened;
        }
        return res;
    }

    @Nonnull
    public static Double[] readBDoubleList(ListNBT doubles) {
        Double[] res = new Double[doubles.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < doubles.size(); i++) {
            INBT nbt = doubles.get(i);
            if (nbt instanceof DoubleNBT) {
                res[i] = ((DoubleNBT) nbt).getDouble();
            } else {
                res[i] = 0.0;
                failed.add(i);
            }
        }
        if (!failed.isEmpty()) {
            Double[] shortened = new Double[res.length - failed.size()];
            int shortenedCount = 0;
            for (int i = 0; i < res.length; i++) {
                if (failed.contains(i))
                    continue;
                shortened[shortenedCount] = res[i];
                ++shortenedCount;
            }
            res = shortened;
        }
        return res;
    }

    @Nonnull
    public static String[] readStringList(ListNBT strings) {
        String[] res = new String[strings.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < strings.size(); i++) {
            INBT nbt = strings.get(i);
            if (nbt instanceof StringNBT) {
                res[i] = nbt.getString();
            } else {
                res[i] = "";
                failed.add(i);
            }
        }
        if (!failed.isEmpty()) {
            String[] shortened = new String[res.length - failed.size()];
            int shortenedCount = 0;
            for (int i = 0; i < res.length; i++) {
                if (failed.contains(i))
                    continue;
                shortened[shortenedCount] = res[i];
                ++shortenedCount;
            }
            res = shortened;
        }
        return res;
    }

    public static boolean[] readBooleanList(ByteArrayNBT booleans) {
        byte[] bytes = booleans.getByteArray();
        boolean[] res = new boolean[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            res[i] = bytes[i] == 0;
        }
        return res;
    }

    public static Boolean[] readBBooleanList(ByteArrayNBT booleans) {
        byte[] bytes = booleans.getByteArray();
        Boolean[] res = new Boolean[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            res[i] = bytes[i] == 0;
        }
        return res;
    }

    public static <T> ListNBT writeIterable(Iterable<T> iterable, Function<? super T, ? extends INBT> serializer) {
        return StreamSupport.stream(iterable.spliterator(), false).map(serializer).collect(toListNBT());
    }

    public static <T> ListNBT writeIterable(Iterable<T> iterable, BiFunction<? super T, Integer, ? extends INBT> serializer) {
        int index = 0;
        ListNBT res = new ListNBT();
        for (T element : iterable) {
            res.add(serializer.apply(element, index));
            index++;
        }
        return res;
    }

    public static <V, T extends INBT> List<V> readList(CollectionNBT<T> list, Function<? super T, ? extends V> deserializer) {
        return list.stream().map(deserializer).collect(Collectors.toList());
    }

    public static <V, T extends INBT> List<V> readList(CollectionNBT<T> list, BiFunction<? super T, Integer, ? extends V> deserializer) {
        List<V> res = new ArrayList<>(list.size());
        int index = 0;
        for (T element : list) {
            res.add(deserializer.apply(element, index));
            index++;
        }
        return res;
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

    public static <T> ListNBT serializeSet(Set<T> set, Function<? super T, ? extends INBT> elementSerializer) {
        ListNBT list = new ListNBT();
        for (T element : set) {
            list.add(elementSerializer.apply(element));
        }
        return list;
    }

    public static <T> Set<T> deserializeSet(ListNBT list, Set<T> toAppendTo, Function<INBT, ? extends T> elementDeserializer) {
        for (INBT nbt : list) {
            toAppendTo.add(elementDeserializer.apply(nbt));
        }
        return toAppendTo;
    }

    /**
     * Connect two {@link ListNBT} together to create a new one. This process has no side effects, which means it
     * will not modify two parameters.
     * <p>
     * Additionally, if any of the lists are empty, the method will directly return the nonempty one. If both of them
     * are empty, it will directly return the second list.
     */
    public static ListNBT concat(ListNBT first, ListNBT second) {
        if (first.isEmpty())
            return second;
        if (second.isEmpty())
            return first;

        return Stream.concat(first.stream(), second.stream()).collect(toListNBT());
    }

    /**
     * If the given stack has a tag, returns it. If the given stack does not have a tag, it will set a reference and
     * return the new tag compound.
     */
    public static CompoundNBT getOrNewTag(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag();
        }
        CompoundNBT tag = new CompoundNBT();
        stack.setTag(tag);
        return tag;
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
