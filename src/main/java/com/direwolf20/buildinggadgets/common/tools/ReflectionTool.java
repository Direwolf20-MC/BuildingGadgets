package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ReflectionTool {
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
        return (instance != null && !ReflectionTool.PREDICATE_STATIC.test(field) && field.getDeclaringClass().isAssignableFrom(instance.getClass())) || (instance == null && ReflectionTool.PREDICATE_STATIC.test(field));
    }

    /**
     * Reflects into ConfigManager, to retrieve the ManagedConfigMapping
     *
     * @return The internal String to {@link Configuration} mappings from the Forge {@link net.minecraftforge.common.config.ConfigManager}. Null on error.
     */
    @Nullable
    public static Map<String, Configuration> getManagedConfigs() {
        try {
            Field configs = ConfigManager.class.getField("CONFIGS");
            configs.setAccessible(true);
            @SuppressWarnings("unchecked") //According to the source, this is the appropriate type
                    Map<String, Configuration> res = (Map<String, Configuration>) configs.get(null);
            return res;
        } catch (NoSuchFieldException e) {
            BuildingGadgets.logger.error("Failed to Hack Forge ConfigManager... Could not find Configuration Objects. Which sadly prevents us from updating the Config File :(.", e);
        } catch (IllegalAccessException e) {
            BuildingGadgets.logger.error("Failed to Hack Forge ConfigManager... It defended itself against retrieving the Configuration Objects! Which sadly prevents us from updating the Config File :(.", e);
        } catch (ClassCastException e) {
            BuildingGadgets.logger.error("Failed to Hack Forge ConfigManager... Configurations have changed their Type - Probly a Forge Update! Which sadly prevents us from updating the Config File :(.", e);
        }
        return null;
    }
}
