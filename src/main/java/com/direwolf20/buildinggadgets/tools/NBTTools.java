package com.direwolf20.buildinggadgets.tools;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import javax.annotation.Nonnull;

public class NBTTools {
    @Nonnull
    public static NBTTagList createDoubleList(double[] doubles) {
        NBTTagList list = new NBTTagList();
        for (double d: doubles) {
            list.appendTag(new NBTTagDouble(d));
        }
        return list;
    }

    @Nonnull
    public static NBTTagList createStringList(String[] strings) {
        NBTTagList list = new NBTTagList();
        for (String s:strings) {
            list.appendTag(new NBTTagString(s));
        }
        return list;
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
            double[] shortened = new double[res.length-failed.size()];
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
            String[] shortened = new String[res.length-failed.size()];
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
}
