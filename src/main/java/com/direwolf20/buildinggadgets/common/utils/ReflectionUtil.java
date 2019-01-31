package com.direwolf20.buildinggadgets.common.utils;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ReflectionUtil {
    public static final Predicate<Field> PREDICATE_STATIC = field -> Modifier.isStatic(field.getModifiers());

    /**
     *
     * @param clazz The class to test
     * @param filter filter to use
     * @return A List containing the Fields declared by this class (superclasses don't count!) who match the given Predicate
     */
    public static List<Field> getFilteredFields(Class<?> clazz, Predicate<Field> filter) {
        return Stream.of(clazz.getDeclaredFields()).filter(filter).collect(ImmutableList.toImmutableList());
    }

    /**
     *
     * @param field The field to test
     * @param instance The instance to check for appropriate nullability
     * @return whether or not the instance is null if the Field is static, or non-null (and of an appropriate class) if the Field is not
     */
    public static boolean isInstanceProvidedForField(@Nonnull Field field, @Nullable Object instance) {
        return (instance != null && !ReflectionUtil.PREDICATE_STATIC.test(field) && field.getDeclaringClass().isAssignableFrom(instance.getClass())) || (instance == null && ReflectionUtil.PREDICATE_STATIC.test(field));
    }
}
