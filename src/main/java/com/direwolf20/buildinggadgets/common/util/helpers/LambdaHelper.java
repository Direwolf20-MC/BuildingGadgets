package com.direwolf20.buildinggadgets.common.util.helpers;

import com.google.common.base.Preconditions;
import net.minecraftforge.common.util.TriPredicate;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class LambdaHelper {

    private LambdaHelper() {}

    public static <T> Predicate<T> and(@Nullable Predicate<T> first, Predicate<T> second) {
        if (first == null)
            return second;
        return first.and(second);
    }

    public static <T, U> BiPredicate<T, U> and(@Nullable BiPredicate<T, U> first, BiPredicate<T, U> second) {
        if (first == null)
            return second;
        return first.and(second);
    }

    public static <T, U, V> TriPredicate<T, U, V> and(@Nullable TriPredicate<T, U, V> first, TriPredicate<T, U, V> second) {
        if (first == null)
            return second;
        return first.and(second);
    }

}
