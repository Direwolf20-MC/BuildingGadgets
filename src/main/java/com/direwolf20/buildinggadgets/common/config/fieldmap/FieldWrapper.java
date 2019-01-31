package com.direwolf20.buildinggadgets.common.config.fieldmap;

import com.direwolf20.buildinggadgets.common.utils.ReflectionUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

@VisibleForTesting
public final class FieldWrapper {
    private final Object instance;
    private final Field field;
    private final FieldMapper<Object,Object> mapper;
    private Object val;

    /**
     * <b>WARNING: USING THIS IS DANGEROUS!</b>
     * This constructs a FieldWrapper under the assumption, that the given FieldMapper applies to the given field.
     * Do not construct otherwise, as hard to trace ClassCastExceptions will be the result!
     */
    public FieldWrapper(@Nonnull Field field, @Nonnull FieldMapper<?,?> mapper) {
        this(field,mapper,null);
    }
    /**
     * <b>WARNING: USING THIS IS DANGEROUS!</b>
     * This constructs a FieldWrapper under the assumption, that the given FieldMapper applies to the given field.
     * Do not construct otherwise, as hard to trace ClassCastExceptions will be the result!
     */
    @SuppressWarnings("unchecked")
    public FieldWrapper(@Nonnull Field field, @Nonnull FieldMapper<?, ?> mapper, @Nullable Object instance) {
        Preconditions.checkArgument(ReflectionUtil.isInstanceProvidedForField(field,instance),
                "Non Static fields must be accessed with an instance! Static fields without! Also watch out for incompatible classes! ");
        this.instance = instance;
        this.field = field;
        this.mapper = (FieldMapper<Object,Object>)mapper;
        this.val = null;
    }

    /**
     * @implNote Performs caching under the assumption that the underlying Field will not change during the life-time of this Object
     * @see Field#get(Object)
     */
    public Object get() throws IllegalAccessException{
        if (val == null)  val = mapper.mapToSync(field.get(instance));
        return val;
    }
    /**
     *
     * @see Field#set(Object, Object)
     */
    public void set(Object val) throws IllegalAccessException{
        field.set(instance,mapper.mapToField(val));
    }

    /**
     *
     * @see Field#getType()
     */
    public Class<?> getType() {
        return field.getType();
    }
}
