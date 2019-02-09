package com.direwolf20.buildinggadgets.common.tools;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class providing additional Methods for reading and writing array's which are not normally provided as NBT-Objects by Minecraft.
 */
public class NBTTool {

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
            list.appendTag(new NBTTagShort(s));
        }
        return list;
    }

    public static NBTTagList createShortList(Short[] shorts) {
        NBTTagList list = new NBTTagList();
        for (short s : shorts) {
            list.appendTag(new NBTTagShort(s));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createFloatList(float[] floats) {
        NBTTagList list = new NBTTagList();
        for (float f : floats) {
            list.appendTag(new NBTTagFloat(f));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createFloatList(Float[] floats) {
        NBTTagList list = new NBTTagList();
        for (Float f : floats) {
            list.appendTag(new NBTTagFloat(f));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createDoubleList(double[] doubles) {
        NBTTagList list = new NBTTagList();
        for (double d : doubles) {
            list.appendTag(new NBTTagDouble(d));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createDoubleList(Double[] doubles) {
        NBTTagList list = new NBTTagList();
        for (Double d : doubles) {
            list.appendTag(new NBTTagDouble(d));
        }
        return list;
    }

    @Nonnull
    public static short[] readShortList(NBTTagList shorts) {
        short[] res = new short[shorts.tagCount()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < shorts.tagCount(); i++) {
            NBTBase nbt = shorts.get(i);
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
        Short[] res = new Short[shorts.tagCount()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < shorts.tagCount(); i++) {
            NBTBase nbt = shorts.get(i);
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
            list.appendTag(new NBTTagString(s));
        }
        return list;
    }

    @Nonnull
    public static float[] readFloatList(NBTTagList floats) {
        float[] res = new float[floats.tagCount()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < floats.tagCount(); i++) {
            NBTBase nbt = floats.get(i);
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
        Float[] res = new Float[floats.tagCount()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < floats.tagCount(); i++) {
            NBTBase nbt = floats.get(i);
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
        double[] res = new double[doubles.tagCount()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < doubles.tagCount(); i++) {
            NBTBase nbt = doubles.get(i);
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
        Double[] res = new Double[doubles.tagCount()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < doubles.tagCount(); i++) {
            NBTBase nbt = doubles.get(i);
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
        String[] res = new String[strings.tagCount()];
        IntList failed = new IntArrayList();
        for (int i = 0; i < strings.tagCount(); i++) {
            NBTBase nbt = strings.get(i);
            if (nbt instanceof NBTTagString) {
                res[i] = ((NBTTagString) nbt).getString();
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

    public static <K, V> NBTTagList serializeMap(Map<K, V> map, Function<K, NBTBase> keySerializer, Function<V, NBTBase> valueSerializer) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("key", keySerializer.apply(entry.getKey()));
            compound.setTag("val", valueSerializer.apply(entry.getValue()));
            list.appendTag(compound);
        }
        return list;
    }

    public static <K, V> Map<K, V> deserializeMap(NBTTagList list, Map<K, V> toAppendTo, Function<NBTBase, K> keyDeserializer, Function<NBTBase, V> valueDeserializer) {
        for (NBTBase nbt : list) {
            if (nbt instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) nbt;
                toAppendTo.put(
                        keyDeserializer.apply(compound.getTag("key")),
                        valueDeserializer.apply(compound.getTag("val"))
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
        if (stack.hasTagCompound()) {
            return stack.getTagCompound();
        }
        NBTTagCompound tag = new NBTTagCompound();
        stack.setTagCompound(tag);
        return tag;
    }

}
