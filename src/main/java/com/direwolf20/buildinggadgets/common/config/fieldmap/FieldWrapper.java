package com.direwolf20.buildinggadgets.common.config.fieldmap;

import com.google.common.annotations.VisibleForTesting;

import java.lang.reflect.Field;

@VisibleForTesting
public final class FieldWrapper {
    private final Field field;
    private final FieldMapper<Object,Object> mapper;

    /**
     * <b>WARNING: USING THIS IS DANGEROUS!</b>
     * This constructs a FieldWrapper under the assumption, that the given FieldMapper applies to the given field.
     * Do not construct otherwise, as hard to trace ClassCastExceptions will be the result!
     */
    @SuppressWarnings("unchecked")
    public FieldWrapper(Field field, FieldMapper<?,?> mapper) {
        this.field = field;
        this.mapper = (FieldMapper<Object,Object>)mapper;
    }

    public Object get() throws IllegalAccessException{
        return mapper.mapToSync(field.get(null));
    }

    public void set(Object val) throws IllegalAccessException{
        field.set(null,mapper.mapToField(val));
    }

    public Class<?> getType() {
        return field.getType();
    }
}
