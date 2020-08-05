package com.direwolf20.buildinggadgets.common.helpers;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;

import java.util.stream.Collector;

public final class NBTHelper {
    private NBTHelper() {}

    public static final Collector<INBT, ListNBT, ListNBT> LIST_COLLECTOR =
            Collector.of(ListNBT::new, ListNBT::add, (l1, l2) -> {l1.addAll(l2); return l1;});
}
