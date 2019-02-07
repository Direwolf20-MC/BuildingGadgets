package com.direwolf20.buildinggadgets.common.config.fieldmap;

import com.direwolf20.buildinggadgets.common.tools.ReflectionTool;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

@VisibleForTesting
public final class FieldWrapper {
    private final Object instance;
    private final Field field;
    private final FieldMapper<Object, Object> mapper;
    private Object val;

    /**
     * <b>WARNING: USING THIS IS DANGEROUS!</b>
     * This constructs a FieldWrapper under the assumption, that the given FieldMapper applies to the given field.
     * Do not construct otherwise, as hard to trace ClassCastExceptions will be the result!
     */
    public FieldWrapper(@Nonnull Field field, @Nonnull FieldMapper<?, ?> mapper) {
        this(field, mapper, null);
    }
    /**
     * <b>WARNING: USING THIS IS DANGEROUS!</b>
     * This constructs a FieldWrapper under the assumption, that the given FieldMapper applies to the given field.
     * Do not construct otherwise, as hard to trace ClassCastExceptions will be the result!
     */
    @SuppressWarnings("unchecked")
    public FieldWrapper(@Nonnull Field field, @Nonnull FieldMapper<?, ?> mapper, @Nullable Object instance) {
        Preconditions.checkArgument(ReflectionTool.isInstanceProvidedForField(field, instance),
                "Non Static fields must be accessed with an instance! Static fields without! Also watch out for incompatible classes! ");
        Preconditions.checkArgument(field.getType().isAssignableFrom(mapper.getFieldType()),
                "The Mapper must map to an assignableField Type! Mapper has Type " + mapper.getFieldType().getName() + " but at least " + field.getType().getName() + " is required!");
        this.instance = instance;
        this.field = field;
        this.mapper = (FieldMapper<Object, Object>) mapper;
        this.val = null;
    }

    /**
     * @implNote Performs caching under the assumption that the underlying Field will not change during the life-time of this Object
     * @see Field#get(Object)
     */
    public <T> T get(Class<T> clazz) throws IllegalAccessException {
        if (!clazz.isAssignableFrom(getMappedType())) {
            throw new IllegalArgumentException("Attempted to retrieve value of type " + clazz.getName() + " but this wrapper only accepts " + mapper.getSyncedType());
        }
        if (val == null) val = mapper.mapToSync(field.get(instance));
        return clazz.cast(val);
    }
    /**
     *
     * @see Field#set(Object, Object)
     */
    public <T> void set(T val, Class<T> clazz) throws IllegalAccessException {
        if (!clazz.isAssignableFrom(getMappedType())) {
            throw new IllegalArgumentException("Attempted to set value of type " + clazz.getName() + " but this wrapper only accepts " + mapper.getSyncedType());
        }
        field.set(instance, mapper.mapToField(val));
    }

    /**
     *
     * @return The type a Field must have, to be usable by this wrapper
     * @see Field#getType()
     */
    public Class<?> getFieldType() {
        return mapper.getFieldType();
    }

    /**
     * @return The type this wrapper accepts values for
     */
    public Class<?> getMappedType() {
        return mapper.getSyncedType();
    }


}
