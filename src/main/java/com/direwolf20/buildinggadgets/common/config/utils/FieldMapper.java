package com.direwolf20.buildinggadgets.common.config.utils;

import java.util.function.Function;

/**
 * Class representing a bijective Function and it's reverse Function used to Mapping Field Types to other Types which can be
 * synced more easily.
 *
 * todo: remove in 1.14
 *
 * The Value produced by {@link #mapToSync(Object)}will then be given to a {@link ITypeSerializer} to be serialized.
 * The value given to {@link #mapToField(Object)} will be the deserialized Value from an appropriate {@link ITypeSerializer}.
 *
 * @param <FieldVal> The Type of Field this Mapper maps to
 * @param <SyncedVal> The Type of Synced Value this Mapper maps to
 */
public class FieldMapper<FieldVal, SyncedVal> {
    public static final String PATTERN_LIST_MAPPER_ID = "Pattern List Mapper";
    public static final FieldMapper<PatternList, String[]> PATTERN_LIST_MAPPER = of(
            PatternList::toArray, PatternList::ofResourcePattern,
            PatternList.class, String[].class);

    private final Function<FieldVal, SyncedVal> fieldToSync;
    private final Function<SyncedVal, FieldVal> syncToField;
    private final Class<FieldVal> fieldType;
    private final Class<SyncedVal> syncedType;

    public static <F> FieldMapper<F, F> id(Class<F> theClass) {
        return of(Function.identity(), Function.identity(), theClass, theClass);
    }

    public static <FieldVal, SyncedVal> FieldMapper<FieldVal, SyncedVal> of(Function<FieldVal, SyncedVal> fieldToSync, Function<SyncedVal, FieldVal> syncToField, Class<FieldVal> fieldType, Class<SyncedVal> syncedType) {
        return new FieldMapper<FieldVal, SyncedVal>(fieldToSync, syncToField, fieldType, syncedType) {};
    }

    private FieldMapper(Function<FieldVal, SyncedVal> fieldToSync, Function<SyncedVal, FieldVal> syncToField, Class<FieldVal> fieldType, Class<SyncedVal> syncedType) {
        this.fieldToSync = fieldToSync;
        this.syncToField = syncToField;
        this.fieldType = fieldType;
        this.syncedType = syncedType;
    }

    public SyncedVal mapToSync(FieldVal val) {
        return fieldToSync.apply(val);
    }

    public FieldVal mapToField(SyncedVal val) {
        return syncToField.apply(val);
    }

    public Class<FieldVal> getFieldType() {
        return fieldType;
    }

    public Class<SyncedVal> getSyncedType() {
        return syncedType;
    }
}
