package com.direwolf20.buildinggadgets.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ReflectionTool {
    public static final Predicate<Field> PREDICATE_STATIC = field -> Modifier.isStatic(field.getModifiers());

    /**
     *
     * @param clazz The class to test
     * @param filter filter to use
     * @return A List containing the Fields declared by this class (superclasses don't count!) who match the given Predicate
     */
    public static List<Field> getFilteredFields(Class<?> clazz, Predicate<Field> filter) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> res = new ArrayList<>(fields.length);
        for (Field field:
             fields) {
            if (filter.test(field))
                res.add(field);
        }
        return res;
    }
}
