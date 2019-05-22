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
import java.util.stream.StreamSupport;

/**
 * Utility class providing additional Methods for reading and writing array's which are not normally provided as NBT-Objects by Minecraft.
 */
public class NBTHelper {

    public static NBTTagByteArray createBooleanList(boolean[] booleans) {
        byte[] bytes = new byte[booleans.length];
        for (int i = 0; i < booleans.length; ++i) {
            bytes[i] = (byte) (booleans[i] ? 0 : 1);
        }
        return new NBTTagByteArray(bytes);
    }

    public static NBTTagByteArray createBooleanList(Boolean[] booleans) {
        byte[] bytes = new byte[booleans.length];
        for (int i = 0; i < booleans.length; ++i) {
            bytes[i] = (byte) (booleans[i] ? 0 : 1);
        }
        return new NBTTagByteArray(bytes);
    }

    public static NBTTagList createShortList(short[] shorts) {
        NBTTagList list = new NBTTagList();
        for (short s : shorts) {
            list.add(new NBTTagShort(s));
        }
        return list;
    }

    public static NBTTagList createShortList(Short[] shorts) {
        NBTTagList list = new NBTTagList();
        for (short s : shorts) {
            list.add(new NBTTagShort(s));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createFloatList(float[] floats) {
        NBTTagList list = new NBTTagList();
        for (float f : floats) {
            list.add(new NBTTagFloat(f));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createFloatList(Float[] floats) {
        NBTTagList list = new NBTTagList();
        for (Float f : floats) {
            list.add(new NBTTagFloat(f));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createDoubleList(double[] doubles) {
        NBTTagList list = new NBTTagList();
        for (double d : doubles) {
            list.add(new NBTTagDouble(d));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createDoubleList(Double[] doubles) {
        NBTTagList list = new NBTTagList();
        for (Double d : doubles) {
            list.add(new NBTTagDouble(d));
        }
        return list;
    }

    @Nonnull
    public static short[] readShortList(NBTTagList shorts) {
        short[] res = new short[shorts.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < shorts.size(); i++) {
            INBTBase nbt = shorts.get(i);
            if (nbt instanceof NBTTagShort) {
                res[i] = ((NBTTagShort) nbt).getShort();
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
    public static Short[] readBShortList(NBTTagList shorts) {
        Short[] res = new Short[shorts.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < shorts.size(); i++) {
            INBTBase nbt = shorts.get(i);
            if (nbt instanceof NBTTagShort) {
                res[i] = ((NBTTagShort) nbt).getShort();
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
    public static NBTTagList createStringList(String[] strings) {
        NBTTagList list = new NBTTagList();
        for (String s : strings) {
            list.add(new NBTTagString(s));
        }
        return list;
    }

    @Nonnull
    public static float[] readFloatList(NBTTagList floats) {
        float[] res = new float[floats.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < floats.size(); i++) {
            INBTBase nbt = floats.get(i);
            if (nbt instanceof NBTTagFloat) {
                res[i] = ((NBTTagFloat) nbt).getFloat();
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
    public static Float[] readBFloatList(NBTTagList floats) {
        Float[] res = new Float[floats.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < floats.size(); i++) {
            INBTBase nbt = floats.get(i);
            if (nbt instanceof NBTTagFloat) {
                res[i] = ((NBTTagFloat) nbt).getFloat();
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
    public static double[] readDoubleList(NBTTagList doubles) {
        double[] res = new double[doubles.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < doubles.size(); i++) {
            INBTBase nbt = doubles.get(i);
            if (nbt instanceof NBTTagDouble) {
                res[i] = ((NBTTagDouble) nbt).getDouble();
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
    public static Double[] readBDoubleList(NBTTagList doubles) {
        Double[] res = new Double[doubles.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < doubles.size(); i++) {
            INBTBase nbt = doubles.get(i);
            if (nbt instanceof NBTTagDouble) {
                res[i] = ((NBTTagDouble) nbt).getDouble();
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
    public static String[] readStringList(NBTTagList strings) {
        String[] res = new String[strings.size()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < strings.size(); i++) {
            INBTBase nbt = strings.get(i);
            if (nbt instanceof NBTTagString) {
                res[i] = nbt.getString();
            }
            else {
                res[i] = "";
                failed.add(i);
            }
        }
        if (! failed.isEmpty()) {
            String[] shortened = new String[res.length - failed.size()];
            int shortenedCount = 0;
            for (int i = 0; i < res.length; i++) {
                if (failed.contains(i))
                    continue;
                shortened[shortenedCount] = res[i];
                ++ shortenedCount;
            }
            res = shortened;
        }
        return res;
    }


    public static boolean[] readBooleanList(NBTTagByteArray booleans) {
        byte[] bytes = booleans.getByteArray();
        boolean[] res = new boolean[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            res[i] = bytes[i] == 0;
        }
        return res;
    }

    public static Boolean[] readBBooleanList(NBTTagByteArray booleans) {
        byte[] bytes = booleans.getByteArray();
        Boolean[] res = new Boolean[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            res[i] = bytes[i] == 0;
        }
        return res;
    }

    public static <T> NBTTagList writeIterable(Iterable<T> iterable, Function<? super T, ? extends INBTBase> serializer) {
        return StreamSupport.stream(iterable.spliterator(), false).map(serializer).collect(toNBTTagList());
    }

    public static <T> NBTTagList writeIterable(Iterable<T> iterable, BiFunction<? super T, Integer, ? extends INBTBase> serializer) {
        int index = 0;
        NBTTagList res = new NBTTagList();
        for (T element : iterable) {
            res.add(serializer.apply(element, index));
            index++;
        }
        return res;
    }

    public static <V, T extends INBTBase> List<V> readList(NBTTagCollection<T> list, Function<? super T, ? extends V> deserializer) {
        return list.stream().map(deserializer).collect(Collectors.toList());
    }

    public static <V, T extends INBTBase> List<V> readList(NBTTagCollection<T> list, BiFunction<? super T, Integer, ? extends V> deserializer) {
        List<V> res = new ArrayList<>(list.size());
        int index = 0;
        for (T element : list) {
            res.add(deserializer.apply(element, index));
            index++;
        }
        return res;
    }

    public static <K, V> NBTTagList serializeMap(Map<K, V> map, Function<? super K, ? extends INBTBase> keySerializer, Function<? super V, ? extends INBTBase> valueSerializer) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag(NBTKeys.MAP_SERIALIZE_KEY, keySerializer.apply(entry.getKey()));
            compound.setTag(NBTKeys.MAP_SERIALIZE_VALUE, valueSerializer.apply(entry.getValue()));
            list.add(compound);
        }
        return list;
    }

    public static <K, V> Map<K, V> deserializeMap(NBTTagList list, Map<K, V> toAppendTo, Function<INBTBase, ? extends K> keyDeserializer, Function<INBTBase, ? extends V> valueDeserializer) {
        for (INBTBase nbt : list) {
            if (nbt instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) nbt;
                toAppendTo.put(
                        keyDeserializer.apply(compound.getTag(NBTKeys.MAP_SERIALIZE_KEY)),
                        valueDeserializer.apply(compound.getTag(NBTKeys.MAP_SERIALIZE_VALUE))
                );
            }
        }
        return toAppendTo;
    }

    /**
     * If the given stack has a tag, returns it. If the given stack does not have a tag, it will set a reference and return the new tag
     * compound.
     */
    public static NBTTagCompound getOrNewTag(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag();
        }
        NBTTagCompound tag = new NBTTagCompound();
        stack.setTag(tag);
        return tag;
    }

    public static <T extends INBTBase> Collector<T, NBTTagList, NBTTagList> toNBTTagList() {
        return new Collector<T, NBTTagList, NBTTagList>() {
            @Override
            public Supplier<NBTTagList> supplier() {
                return NBTTagList::new;
            }

            @Override
            public BiConsumer<NBTTagList, T> accumulator() {
                return NBTTagList::add;
            }

            @Override
            public BinaryOperator<NBTTagList> combiner() {
                return (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                };
            }

            @Override
            public Function<NBTTagList, NBTTagList> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.of(Characteristics.IDENTITY_FINISH, Characteristics.UNORDERED);
            }
        };
    }

}
